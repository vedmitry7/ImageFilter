package app.mycity.filterimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.zomato.photofilters.utils.GeneralUtils;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.ArrayList;
import java.util.List;

public class MyThumbnailsManager {

    private static List<ThumbnailItem> filterThumbs = new ArrayList<>(10);
    private static List<ThumbnailItem> processedThumbs = new ArrayList<>(10);
    static int maxPixSize;

    private MyThumbnailsManager() {
    }

    public static void addThumb(ThumbnailItem thumbnailItem) {
        filterThumbs.add(thumbnailItem);
    }

    public static List<ThumbnailItem> processThumbs(Context context) {
        for (ThumbnailItem thumb : filterThumbs) {

            if(maxPixSize == 0){
                maxPixSize = (int) BitmapUtils.convertDpToPixel(80, context);
            }

            final int maxSize =  maxPixSize;
            float outWidth;
            float outHeight;
            float inWidth = thumb.image.getWidth();
            float inHeight = thumb.image.getHeight();

            if(inWidth > inHeight){
                outHeight = maxSize;
                outWidth = inWidth / (inHeight/maxSize);
            } else {
                outWidth = maxSize;
                outHeight = inHeight / (inWidth/maxSize);
            }

            thumb.image = Bitmap.createScaledBitmap(thumb.image, (int) outWidth, (int) outHeight, false);
            thumb.image = thumb.filter.processFilter(thumb.image);
            // cropping circle

            // TODO - think about circular thumbnails
           // thumb.image = GeneralUtils.generateCircularBitmap(thumb.image);
            processedThumbs.add(thumb);
        }
        return processedThumbs;
    }

    public static void clearThumbs() {
        filterThumbs = new ArrayList<>();
        processedThumbs = new ArrayList<>();
    }
}
