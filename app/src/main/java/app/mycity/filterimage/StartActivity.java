package app.mycity.filterimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {

    File file;
    Uri fileUri;

    private static final int REQUEST_CODE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
    }


    public void makeCheckin(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File("/storage/emulated/0/"+"test.png");
        fileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            //   Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("path", file.getAbsolutePath());
                Log.d("TAG21", file.getAbsolutePath());
                startActivity(intent);
                this.finish();
        }
    }
}

