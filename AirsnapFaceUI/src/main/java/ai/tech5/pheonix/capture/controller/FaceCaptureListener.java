package ai.tech5.pheonix.capture.controller;


import com.phoenixcapture.camerakit.FaceBox;

public interface FaceCaptureListener {

     void onFaceCaptured(byte[] data,byte[] originalData, FaceBox faceBox);

     void OnFaceCaptureFailed(String errorMessage);

     void onCancelled();

     void onTimedout(byte[] faceImage);
}
