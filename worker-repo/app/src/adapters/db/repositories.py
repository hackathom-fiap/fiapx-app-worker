from sqlalchemy.orm import Session
from src.domain.entities import Video as VideoEntity, VideoStatus
from src.ports.interfaces import VideoRepository
import models

class PostgresVideoRepository(VideoRepository):
    def __init__(self, db: Session):
        self.db = db

    def get_by_id(self, video_id: int) -> VideoEntity:
        db_video = self.db.query(models.Video).filter(models.Video.id == video_id).first()
        if db_video:
            return VideoEntity(
                id=db_video.id,
                filename=db_video.filename,
                status=VideoStatus(db_video.status.value),
                zip_path=db_video.zip_path
            )
        return None

    def update_status(self, video: VideoEntity):
        db_video = self.db.query(models.Video).filter(models.Video.id == video.id).first()
        if db_video:
            db_video.status = models.VideoStatus(video.status.value)
            db_video.zip_path = video.zip_path
            self.db.commit()

    def get_user_email_by_video(self, video_id: int) -> str:
        db_video = self.db.query(models.Video).filter(models.Video.id == video_id).first()
        if db_video and db_video.owner:
            return db_video.owner.email
        return None
