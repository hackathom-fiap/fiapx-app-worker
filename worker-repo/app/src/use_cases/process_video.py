from src.domain.entities import VideoStatus
from src.ports.interfaces import VideoRepository, VideoProcessor, NotificationService
import os
import time
from prometheus_client import Counter, Histogram
import boto3 # Import boto3 for S3 exceptions

class ProcessVideoUseCase:
    def __init__(self, video_repo: VideoRepository, processor: VideoProcessor, notifier: NotificationService, 
                 uploads_dir: str, outputs_dir: str,
                 videos_processed_metric: Counter, video_processing_errors_metric: Counter,
                 video_processing_duration_metric: Histogram,
                 s3_client, s3_bucket_name: str): # Add S3 dependencies
        self.video_repo = video_repo
        self.processor = processor
        self.notifier = notifier
        self.uploads_dir = uploads_dir
        self.outputs_dir = outputs_dir
        self.videos_processed_metric = videos_processed_metric
        self.video_processing_errors_metric = video_processing_errors_metric
        self.video_processing_duration_metric = video_processing_duration_metric
        self.s3_client = s3_client
        self.s3_bucket_name = s3_bucket_name


    def execute(self, video_id: int, filename: str):
        start_time = time.time()
        print(f"Executing use case for video {video_id}")
        
        # 1. Update status to PROCESSING and increment metric
        video = self.video_repo.get_by_id(video_id)
        if not video:
            raise ValueError(f"Video {video_id} not found")
        
        video.status = VideoStatus.PROCESSING
        self.video_repo.update_status(video)
        self.videos_processed_metric.labels(status='processing').inc()

        zip_local_path = None # Initialize to None for finally block cleanup
        try:
            # 2. Process Video
            video_path = os.path.join(self.uploads_dir, filename)
            if not os.path.exists(video_path):
                raise FileNotFoundError(f"File {video_path} does not exist")

            zip_filename = self.processor.extract_frames_to_zip(video_path, self.outputs_dir)
            zip_local_path = os.path.join(self.outputs_dir, zip_filename)
            
            # 3. Upload to S3
            s3_key = f"processed_videos/{video_id}/{zip_filename}"
            print(f"Uploading {zip_local_path} to S3 bucket {self.s3_bucket_name} with key {s3_key}")
            self.s3_client.upload_file(zip_local_path, self.s3_bucket_name, s3_key)
            
            s3_url = f"https://{self.s3_bucket_name}.s3.amazonaws.com/{s3_key}"
            print(f"File uploaded to S3: {s3_url}")

            # 4. Update status to COMPLETED and increment metric
            video.status = VideoStatus.COMPLETED
            video.zip_path = s3_url # Store S3 URL instead of local path
            self.video_repo.update_status(video)
            self.videos_processed_metric.labels(status='completed').inc()
            
        except boto3.exceptions.ClientError as s3_error:
            print(f"S3 upload error: {s3_error}")
            video.status = VideoStatus.ERROR
            self.video_repo.update_status(video)
            self.videos_processed_metric.labels(status='error').inc()
            self.video_processing_errors_metric.inc()
            
            user_email = self.video_repo.get_user_email_by_video(video_id)
            if user_email:
                self.notifier.notify_error(user_email, filename, f"S3 Upload Failed: {str(s3_error)}")
            raise s3_error

        except Exception as e:
            print(f"Error processing video: {e}")
            video.status = VideoStatus.ERROR
            self.video_repo.update_status(video)
            self.videos_processed_metric.labels(status='error').inc()
            self.video_processing_errors_metric.inc()
            
            # 4. Notify User
            user_email = self.video_repo.get_user_email_by_video(video_id)
            if user_email:
                self.notifier.notify_error(user_email, filename, str(e))
            
            raise e
        finally:
            # Cleanup local ZIP file after processing/upload
            if zip_local_path and os.path.exists(zip_local_path):
                os.remove(zip_local_path)
                print(f"Cleaned up local ZIP file: {zip_local_path}")
            end_time = time.time()
            self.video_processing_duration_metric.observe(end_time - start_time)
