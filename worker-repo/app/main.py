import pika
import json
import os
import time
import sys
import threading
import boto3 # Import boto3
from database import SessionLocal
from src.adapters.db.repositories import PostgresVideoRepository
from src.adapters.video.opencv_processor import OpenCVVideoProcessor
from src.adapters.notification.log_service import LogNotificationService
from src.adapters.notification.ses_service import SESNotificationService
from src.use_cases.process_video import ProcessVideoUseCase

# Prometheus Metrics
from prometheus_client import start_http_server, Counter, Histogram

# Configurações
RABBITMQ_URL = os.getenv("RABBITMQ_URL", "amqp://user:password@rabbitmq:5672/")
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
    
    while True:
        try:
            params = pika.URLParameters(RABBITMQ_URL)
            connection = pika.BlockingConnection(params)
            channel = connection.channel()
            break
        except pika.exceptions.AMQPConnectionError:
            print("RabbitMQ indisponível, tentando novamente em 5s...")
            time.sleep(5)
            
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
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

