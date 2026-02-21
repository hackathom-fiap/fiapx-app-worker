from src.ports.interfaces import NotificationService

class LogNotificationService(NotificationService):
    def notify_error(self, email: str, video_filename: str, error_message: str):
        print("============== SIMULAÇÃO DE EMAIL ==============")
        print(f"PARA: {email}")
        print(f"ASSUNTO: Erro no processamento do vídeo: {video_filename}")
        print(f"MENSAGEM: Ocorreu um erro: {error_message}")
        print("================================================")
