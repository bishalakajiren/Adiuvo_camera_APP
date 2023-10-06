package com.example.adiuvo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CaptureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CaptureFragment extends Fragment {

    private TextureView textureView;
    private Button captureButton;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;

    private MediaRecorder mediaRecorder;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;

    private File imageFile; // File to save captured images
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageReader imageReader;
    private Size previewSize;
    private int sensorOrientation;

    private ToggleButton modeToggleButton; // Add ToggleButton

    private boolean isVideoMode = false;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public CaptureFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CaptureFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CaptureFragment newInstance(String param1, String param2) {
        CaptureFragment fragment = new CaptureFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        cameraManager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capture, container, false);

        textureView = view.findViewById(R.id.texture_view);
        captureButton = view.findViewById(R.id.capture_button);
        modeToggleButton = view.findViewById(R.id.mode_toggle_button);

        Button stopButton = view.findViewById(R.id.stop_button);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        modeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVideoMode = isChecked;
            }
        });

        // Set up a click listener for the capture button
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoMode) {
                    captureVideo(); // Implement this method to capture video
                } else {
                    capturePhoto(); // Implement this method to capture a photo
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera(); // Open the camera when the fragment is resumed
    }

    @Override
    public void onPause() {
        closeCamera(); // Release camera resources when the fragment is paused
        super.onPause();
    }

    private void openCamera() {
        // Initialize cameraManager and open camera
        // Implement this method to handle camera initialization

        CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0]; // Use the first available camera

            // Check if the app has the CAMERA permission
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Request CAMERA permission if not granted
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map != null) {
                setUpCameraOutputs(map, cameraId);
                configureTransform(textureView.getWidth(), textureView.getHeight());
                manager.openCamera(cameraId, cameraStateCallback, null);
                Log.d("CaptureFragment", "Capture session configured and preview started");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e("CaptureFragment", "Error starting preview: " + e.getMessage());
        }
    }
    private void setUpCameraOutputs(StreamConfigurationMap map, String cameraId) {
        // Choose the output size for the camera preview
        previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), textureView.getWidth(), textureView.getHeight());

        // Create an ImageReader to capture still images
        int MAX_IMAGES = 2;
        imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, MAX_IMAGES);
        imageReader.setOnImageAvailableListener(imageReaderListener, null);

        // Calculate the rotation needed for the camera sensor
        int displayRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        sensorOrientation = getSensorOrientation(cameraId, displayRotation);

        // Additional camera setup code (if needed)
        try {
            // Check if the camera has auto-focus capability
            Boolean hasAutoFocus = map.getOutputSizes(ImageFormat.JPEG) != null && map.getOutputSizes(ImageFormat.JPEG).length > 0;
            if (hasAutoFocus) {
                // Enable auto-focus for the camera capture
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            }

            // Add more setup code as per your requirements

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == previewSize) {
            return;
        }
        int rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // This is called when the camera is opened. Set cameraDevice to the opened camera.
            cameraDevice = camera;

            // Create a capture session once the camera is opened
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // This is called when the camera is disconnected. Close the cameraDevice.
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            // This is called if there is an error opening the camera. Close the cameraDevice.
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // Set the default buffer size to the preview size
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // Create a Surface from the SurfaceTexture
            Surface surface = new Surface(texture);

            // Create a CaptureRequest.Builder
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            // Add the Surface as the target for the CaptureRequest
            captureRequestBuilder.addTarget(surface);

            // Create a CameraCaptureSession to handle camera preview
            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            // The camera is already closed
                            if (cameraDevice == null) return;

                            // When the session is ready, start displaying the preview
                            cameraCaptureSession = session;
                            try {
                                // Set the auto-focus mode
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                // Start displaying the camera preview
                                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            // Handle configuration failures
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        // Implement your logic to choose the optimal size here
        // This code will depend on your specific requirements
        // For example, you can choose the size that is closest to the desired width and height
        // or you can implement custom logic based on your app's needs

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : choices) {
            if (size.getWidth() == width && size.getHeight() == height) {
                return size;
            }

            double aspectRatio = (double) size.getWidth() / (double) size.getHeight();
            double diff = Math.abs(aspectRatio - (double) width / (double) height);

            if (diff < minDiff) {
                optimalSize = size;
                minDiff = diff;
            }
        }

        return optimalSize;
    }

    private int getSensorOrientation(String cameraId, int displayRotation) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            // Adjust sensor orientation based on the display rotation
            int deviceOrientation = ORIENTATIONS.get(displayRotation);
            return (sensorOrientation + deviceOrientation + 360) % 360;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if there is an error
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openCamera();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(requireContext(), "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void closeCamera() {
        // Release camera resources
        // Implement this method to handle camera closure
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void capturePhoto() {
        Log.d("CaptureFragment", "Capture photo button pressed");
        // Implement this method to capture a photo
        if (cameraDevice == null) return;

        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            int rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    // Image captured, process it here (e.g., save to a file)
                }
            };

            cameraCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private void captureVideo() {
        if (cameraDevice == null) return;

        try {
            mediaRecorder = new MediaRecorder();

            // Set up MediaRecorder configuration
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

            // Set the output file for the video
            File videoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "video.mp4");
            mediaRecorder.setOutputFile(videoFile.getAbsolutePath());

            // Set the orientation hint based on the device orientation
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation);
            mediaRecorder.setOrientationHint(orientation);

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;

            // Release the camera if it was being used for recording
            if (isVideoMode) {
                closeCamera();
                openCamera(); // Re-open the camera for preview
            }
        }
    }
    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360;
    }

    private final ImageReader.OnImageAvailableListener imageReaderListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    // Process the image
                    // For example, you can save it to a file
                    File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "captured_image.jpg");
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    FileOutputStream output = new FileOutputStream(imageFile);
                    output.write(bytes);
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }
    };

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
}