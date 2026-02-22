import pika
import json
import os
import time
import sys
import threading
import boto3 # Import boto3
import ssl # Add this import
from database import SessionLocal
from src.adapters.db.repositories import PostgresVideoRepository
from src.adapters.video.opencv_processor import OpenCVVideoProcessor
from src.adapters.notification.log_service import LogNotificationService
from src.adapters.notification.ses_service import SESNotificationService
from src.use_cases.process_video import ProcessVideoUseCase
from urllib.parse import quote_plus # Add this import
from urllib.parse import urlparse # Add this import

# Prometheus Metrics
from prometheus_client import start_http_server, Counter, Histogram

# Configurações
MQ_USER = os.getenv("MQ_USER", "user")
MQ_PASSWORD = os.getenv("MQ_PASSWORD", "password")
MQ_HOST = os.getenv("MQ_HOST", "rabbitmq")
MQ_PORT = os.getenv("MQ_PORT", "5671") # Changed default to 5671 for Amazon MQ with TLS

encoded_mq_password = quote_plus(MQ_PASSWORD)
RABBITMQ_URL = f"amqps://{MQ_USER}:{encoded_mq_password}@{MQ_HOST}:{MQ_PORT}/"
QUEUE_NAME = "video_processing"
SHARED_DIR = os.getenv("SHARED_DIR", "/data")
UPLOADS_DIR = os.path.join(SHARED_DIR, "uploads")
OUTPUTS_DIR = os.path.join(SHARED_DIR, "outputs")
NOTIFICATION_METHOD = os.getenv("NOTIFICATION_METHOD", "LOG") # LOG ou SES
PROMETHEUS_METRICS_PORT = int(os.getenv("PROMETHEUS_METRICS_PORT", 8001))
S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME") # Get S3 bucket name from environment

# Garantir que diretórios existam
os.makedirs(UPLOADS_DIR, exist_ok=True)
os.makedirs(OUTPUTS_DIR, exist_ok=True)

# S3 Client
s3_client = boto3.client('s3') # Instantiate S3 client

# Metrics
VIDEOS_PROCESSED = Counter(
    "videos_processed_total", "Total videos processed", ["status"]
)
VIDEO_PROCESSING_ERRORS = Counter(
    "video_processing_errors_total", "Total errors during video processing"
)
VIDEO_PROCESSING_DURATION_SECONDS = Histogram(
    "video_processing_duration_seconds", "Duration of video processing in seconds"
)


def callback(ch, method, properties, body):
    print(f" [x] Recebido: {body}")
    data = json.loads(body)
    
    video_id = data.get("video_id")
    filename = data.get("filename")
    
    if not video_id or not filename:
        ch.basic_ack(delivery_tag=method.delivery_tag)
        return

    # Dependency Injection
    db_session = SessionLocal()
    try:
        repo = PostgresVideoRepository(db_session)
        processor = OpenCVVideoProcessor()
        
        if NOTIFICATION_METHOD == "SES":
            notifier = SESNotificationService()
        else:
            notifier = LogNotificationService()
            
        use_case = ProcessVideoUseCase(
            repo, processor, notifier, UPLOADS_DIR, OUTPUTS_DIR,
            VIDEOS_PROCESSED, VIDEO_PROCESSING_ERRORS, VIDEO_PROCESSING_DURATION_SECONDS,
            s3_client, S3_BUCKET_NAME # Pass s3_client and S3_BUCKET_NAME
        )
        
        use_case.execute(video_id, filename)
        print(f" [x] Processamento concluído para vídeo ID {video_id}")
        
    except Exception as e:
        print(f"Critical error in worker: {e}")
    finally:
        db_session.close()
        ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
    # Start up the server to expose the metrics.
    print(f"🎬 Starting Prometheus metrics server on port {PROMETHEUS_METRICS_PORT}")
    start_http_server(PROMETHEUS_METRICS_PORT)
    
    print("🎬 Video Worker connecting to RabbitMQ (Clean Arch)...")
    
    print("--- Tracing: Parsing RABBITMQ_URL ---")
    url_components = urlparse(RABBITMQ_URL)
    print(f"--- Tracing: Parsed URL components - Host: {url_components.hostname}, Port: {url_components.port}, User: {url_components.username} ---")
    
    mq_user = url_components.username
    mq_password = url_components.password
    mq_host = url_components.hostname
    mq_port = url_components.port if url_components.port else 5671 # Default to 5671 for amqps

    print("--- Tracing: Creating SSLOptions ---")
    
    # Create an SSLContext object
    context = ssl.create_default_context()
    context.check_hostname = False # Para cert_reqs=ssl.CERT_NONE, a verificação de hostname é desabilitada.
                                  # Re-habilitar e configurar para produção.
    context.verify_mode = ssl.CERT_NONE # Para teste inicial, desabilitar validação de certificado.

    ssl_options = pika.SSLOptions(
        context=context
    )
    print("--- Tracing: SSLOptions created ---")

    credentials = pika.PlainCredentials(mq_user, mq_password)
    print("--- Tracing: PlainCredentials created ---")
    
    connection_parameters = pika.ConnectionParameters(
        host=mq_host,
        port=mq_port,
        credentials=credentials,
        virtual_host="/", # Amazon MQ uses "/" as virtual host
        ssl_options=ssl_options
    )
    print("--- Tracing: ConnectionParameters created ---")

    while True:
        try:
            print("--- Tracing: Attempting BlockingConnection ---")
            connection = pika.BlockingConnection(connection_parameters)
            print("--- Tracing: BlockingConnection established ---")
            channel = connection.channel()
            print("--- Tracing: Channel established ---")
            break
        except pika.exceptions.AMQPConnectionError as e:
            print(f"--- Tracing: AMQPConnectionError caught: {repr(e)} ---")
            print(f"RabbitMQ indisponível, tentando novamente em 5s... Erro detalhado: {repr(e)}")
            time.sleep(5)
            
    print("--- Tracing: Connection loop exited ---")
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
    print("--- Tracing: Queue declared ---")
    channel.basic_qos(prefetch_count=1)
    
    print(' [*] Waiting for messages. To exit press CTRL+C')
    channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback)
    
    channel.start_consuming()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)

