import boto3
from botocore.exceptions import ClientError
from src.ports.interfaces import NotificationService

class SESNotificationService(NotificationService):
    def __init__(self, region_name="us-east-1", sender_email="noreply@fiapx.com"):
        self.client = boto3.client('ses', region_name=region_name)
        self.sender = sender_email

    def notify_error(self, email: str, video_filename: str, error_message: str):
        try:
            self.client.send_email(
                Source=self.sender,
                Destination={'ToAddresses': [email]},
                Message={
                    'Subject': {'Data': f"Erro no processamento do vídeo: {video_filename}"},
                    'Body': {
                        'Text': {'Data': f"Olá,\n\nOcorreu um erro ao processar seu vídeo '{video_filename}'.\n\nDetalhes: {error_message}"}
                    }
                }
            )
            print(f"📧 Email enviado para {email} via SES.")
        except ClientError as e:
            print(f"❌ Erro ao enviar email via SES: {e}")
