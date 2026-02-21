from abc import ABC, abstractmethod
from src.domain.entities import Video

class VideoRepository(ABC):
    @abstractmethod
    def get_by_id(self, video_id: int) -> Video:
        pass

    @abstractmethod
    def update_status(self, video: Video):
        pass

    @abstractmethod
    def get_user_email_by_video(self, video_id: int) -> str:
        pass

class VideoProcessor(ABC):
    @abstractmethod
    def extract_frames_to_zip(self, input_path: str, output_dir: str) -> str:
        """Processa o vídeo e retorna o nome do arquivo ZIP gerado."""
        pass

class NotificationService(ABC):
    @abstractmethod
    def notify_error(self, email: str, video_filename: str, error_message: str):
        pass
