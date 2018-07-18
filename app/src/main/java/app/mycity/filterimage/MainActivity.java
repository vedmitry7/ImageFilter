package app.mycity.filterimage;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements FiltersListFragment.FiltersListFragmentListener, EditImageFragment.EditImageFragmentListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String IMAGE_NAME = "dog.jpg";

    @BindView(R.id.image_preview)
    ImageView imagePreview;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    Filter selectedFilter;
    Filter editFilter;

    Bitmap outFinalImage;
    Bitmap superOriginalImage;
    Bitmap outFilteredImage;

    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;

    // the final image after applying
    // brightness, saturation, contrast
    Bitmap finalImage;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    // modified image values
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;

    File file;
    Uri fileUri;

    String path;

    private static final int REQUEST_CODE = 1;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Log.d("TAG21", "MAin OnCreate");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.activity_title_main));

        path = getIntent().getStringExtra("path");

        loadImage();
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    @OnClick(R.id.image_preview)
    public void clickImage(View v){
       ImageView.ScaleType scaleType = imagePreview.getScaleType();
        Log.d("TAG21", "ckick " + scaleType);
    }

    // load the default image from assets on app launch
    private void loadImage() {

        Bitmap bitmap =  BitmapFactory.decodeFile(path, new BitmapFactory.Options());
        Log.d("TAG21", "come - " + bitmap.getWidth() + " : " + bitmap.getHeight());



        originalImage = Bitmap.createScaledBitmap(bitmap,(int) bitmap.getWidth()/4, (int) bitmap.getHeight()/4, false);
        Log.d("TAG21", "S will using = " + originalImage.getWidth() + " : " + originalImage.getHeight());
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(originalImage);


        final int maxSize = 1920;
        float outWidth;
        float outHeight;
        float inWidth = bitmap.getWidth();
        float inHeight = bitmap.getHeight();

        if(inWidth < inHeight){
            outHeight = maxSize;
            outWidth = inWidth / (inHeight/maxSize);
        } else {
            outWidth = maxSize;
            outHeight = inHeight / (inWidth/maxSize);
        }

        superOriginalImage = Bitmap.createScaledBitmap(bitmap,(int) outWidth, (int) outHeight, false);
        Log.d("TAG21", "S  super orig = " + superOriginalImage.getWidth() + " : " + superOriginalImage.getHeight());
        outFinalImage = superOriginalImage.copy(Bitmap.Config.ARGB_8888, true);
        outFilteredImage = superOriginalImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // adding filter list fragment
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.addImage(originalImage);
        filtersListFragment.setListener(this);

        // adding edit image fragmentp
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(filtersListFragment, getString(R.string.tab_filters));
        adapter.addFragment(editImageFragment, getString(R.string.tab_edit));

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        // reset image controls
        resetControls();
        //save for out image
        selectedFilter = filter;

        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);


    }

    @Override
    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
    }

    @Override
    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
    }

    @Override
    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
    }

    @Override
    public void onEditStarted() {
    }

    @Override
    public void onEditCompleted() {
        final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
        Filter editFilter = new Filter();
        editFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        editFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        editFilter.addSubFilter(new SaturationSubfilter(saturationFinal));

        this.editFilter = editFilter;
        finalImage = editFilter.processFilter(bitmap);
        imagePreview.setImageBitmap(finalImage);

    }

    /**
     * Resets image edit controls to normal when new filter
     * is selected
     */
    private void resetControls() {
        if (editImageFragment != null) {
            editImageFragment.resetControls();
        }
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;

        editFilter = null;

    }

    public void makeCheckin() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File("/storage/emulated/0/"+"test.png");
        fileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /*
     * saves image to camera gallery
     * */
    private void saveImageToGallery() {

        outFilteredImage = superOriginalImage.copy(Bitmap.Config.ARGB_8888, true);

        if(selectedFilter!=null)
            selectedFilter.processFilter(outFilteredImage);

        outFinalImage = outFilteredImage.copy(Bitmap.Config.ARGB_8888, true);

        final Bitmap bitmap = outFilteredImage.copy(Bitmap.Config.ARGB_8888, true);

        if(editFilter!=null)
        outFinalImage = editFilter.processFilter(bitmap);

        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), outFinalImage, "/storage/emulated/0/test.jpg", null);
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Image saved to gallery!", Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openImage(path);
                                            }
                                        });

                                snackbar.show();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Unable to save image!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    // opening image in default image viewer app
    private void openImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

            // clear bitmap memory
            originalImage.recycle();
            finalImage.recycle();
            finalImage.recycle();

            loadImage();
            selectedFilter = null;
            resetControls();
            filtersListFragment.reloadIndicator();

            filtersListFragment.prepareThumbnail(originalImage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            // openImageFromGallery();
            makeCheckin();
            return true;
        }

        if (id == R.id.action_save) {
            saveImageToGallery();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}