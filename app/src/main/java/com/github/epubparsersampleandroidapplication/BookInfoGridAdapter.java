package com.github.epubparsersampleandroidapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mert on 08.09.2016.
 */
public class BookInfoGridAdapter extends BaseAdapter {

    private Context context;
    private List<BookInfo> bookInfoList;

    public BookInfoGridAdapter(Context context, List<BookInfo> bookInfoList) {
        this.context = context;
        this.bookInfoList = bookInfoList;
    }

    private final class ViewHolder {
        public TextView title;
        public ImageView coverImage;
    }

    @Override
    public int getCount() {
        return bookInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return bookInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.book_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.txt_book_title);
            viewHolder.coverImage = (ImageView) convertView.findViewById(R.id.img_cover);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(bookInfoList.get(position).getTitle());

        boolean isCoverImageNotExists = bookInfoList.get(position).isCoverImageNotExists();

        if (!isCoverImageNotExists) { // Not searched for coverImage for this position yet. It may exist.
            Bitmap savedBitmap = bookInfoList.get(position).getCoverImageBitmap();

            if (savedBitmap != null) {
                viewHolder.coverImage.setImageBitmap(savedBitmap);
            } else {
                byte[] coverImageAsBytes = bookInfoList.get(position).getCoverImage();

                if (coverImageAsBytes != null) {
                    Bitmap bitmap = decodeBitmapFromByteArray(coverImageAsBytes, 100, 200);

                    bookInfoList.get(position).setCoverImageBitmap(bitmap);
                    bookInfoList.get(position).setCoverImage(null);

                    viewHolder.coverImage.setImageBitmap(bitmap);
                } else { // Searched and not found.
                    bookInfoList.get(position).setCoverImageNotExists(true);
                    viewHolder.coverImage.setImageResource(android.R.drawable.alert_light_frame);
                }
            }
        } else { // Searched before and not found.
            viewHolder.coverImage.setImageResource(android.R.drawable.alert_light_frame);
        }

        return convertView;
    }

    private Bitmap decodeBitmapFromByteArray(byte[] coverImage, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(coverImage, 0, coverImage.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(coverImage, 0, coverImage.length, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
