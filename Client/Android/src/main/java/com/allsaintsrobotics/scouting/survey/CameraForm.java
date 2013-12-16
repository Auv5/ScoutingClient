package com.allsaintsrobotics.scouting.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.allsaintsrobotics.scouting.R;
import com.allsaintsrobotics.scouting.ScoutEdit;
import com.allsaintsrobotics.scouting.models.Team;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jack on 11/27/13.
 */
public class CameraForm extends Form {
    private static final int TAKE_PHOTO_CODE = 15;

    private View view = null;
    private TextView label;
    private File finalImage;
    private ImageView imgView;

    private File selectedImage = null;
    private File firstCacheLoc = null;

    private int savedScroll = 0;

    public CameraForm(Question q, Team t, File location) {
        super(q, t);
        this.finalImage = location;
    }

    @Override
    public View getAnswerView(final ScoutEdit c, ViewGroup parent) {
        if (view == null)
        {
            LayoutInflater li = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.view = li.inflate(R.layout.question_camera, null);
        }

        this.label = (TextView) view.findViewById(R.id.cam_label);

        ImageButton takePic = (ImageButton) view.findViewById(R.id.take_pic_button);

        this.imgView = (ImageView) view.findViewById(R.id.pic_preview);

        final File cacheTo = new File(Environment.getExternalStorageDirectory(), "tmp/" + finalImage.getName());

        new File(cacheTo.getParent()).mkdirs();

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cacheTo));

                savedScroll = c.getScrollPos();

                (c).startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);


                CameraForm.this.firstCacheLoc = cacheTo;
            }
        });

        if (finalImage.exists()) {
            imgView.setImageBitmap(decodeSampledBitmapFromFile(finalImage.getAbsolutePath(),
                    imgView.getWidth()));
        }

        return view;
    }

    @Override
    public boolean result(ScoutEdit c, int request, int response, Intent data) {
        if (request == TAKE_PHOTO_CODE && response == Activity.RESULT_OK) {
            // Set the currently selected image.
//            CameraForm.this.selectedImage = new File(data.getData().getPath());

            this.selectedImage = new File(c.getCacheDir(), firstCacheLoc.getName());

            try {
                this.copyFile(firstCacheLoc, selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            firstCacheLoc.delete();

            imgView.setImageBitmap(decodeSampledBitmapFromFile(selectedImage.getAbsolutePath(),
                    imgView.getWidth()));

            c.setScrollPos(savedScroll);

            // Handled
            return true;
        }

        return super.result(c, request, response, data);
    }

    @Override
    public String getAnswer() {
        return finalImage.exists() ? finalImage.toString() : null;
    }

    @Override
    public void setError(String error) {
        label.setError(error);
    }

    private boolean copyFile(File src,File dst)throws IOException {
        if(src.getAbsolutePath().equals(dst.getAbsolutePath())) {
            return true;
        }

        else {
            InputStream is = new FileInputStream(src);
            OutputStream os= new FileOutputStream(dst);
            byte[] buff=new byte[1024];
            int len;
            while((len=is.read(buff))>0){
                os.write(buff,0,len);
            }
            is.close();
            os.close();
        }

        return true;
    }

    private static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        // Scale to the ratio of the original.
        int reqHeight = (height*reqWidth)/width;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    @Override
    public void write() {
        try {
            if (selectedImage != null) {
                this.copyFile(selectedImage, finalImage);
                selectedImage.delete();
            }
        } catch (IOException e) {
            //TODO: Handle with loggers.
            e.printStackTrace();
        }

        super.write();
    }

    public static class CameraFormFactory extends FormFactory {
        private static final String TAG = "CameraFormFactory";

        public CameraFormFactory() {}

        @Override
        public Form getForm(Question q, Team t) {
            String dir = "scouting_pics";

            File sdcard = Environment.getExternalStorageDirectory();

            //TODO: Handle no SD card case

            File dirFile = new File(sdcard, dir);

            // Returns false if dir already was there.
            // Note: We don't care. We just want to make sure it exists.
            dirFile.mkdir();

            String label = q.getLabel();

            //TODO: Add any more invalid characters here.
            String filename = label.replace(" ", "_").replace("/", "").replace(".", "").
                    replace(",", "").replace("'", "") + ".jpg";
            
            Log.d(TAG, "Camera filename: " + filename);

            File newFile = new File(dirFile, filename);

            return new CameraForm(q, t, newFile);
        }
    }
}