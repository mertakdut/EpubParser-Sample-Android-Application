package com.github.epubparsersampleandroidapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mertakdut.BookSection;
import com.github.mertakdut.CssStatus;
import com.github.mertakdut.Reader;
import com.github.mertakdut.exception.OutOfPagesException;
import com.github.mertakdut.exception.ReadingException;

public class MainActivity extends AppCompatActivity implements PageFragment.OnFragmentReadyListener {

    private Reader reader;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * (FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private int pageCount = Integer.MAX_VALUE;
    private int pxScreenWidth;

    private boolean isPickedWebView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pxScreenWidth = getResources().getDisplayMetrics().widthPixels;

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (getIntent() != null && getIntent().getExtras() != null) {
            String filePath = getIntent().getExtras().getString("filePath");
            isPickedWebView = getIntent().getExtras().getBoolean("isWebView");

            try {
                reader = new Reader();
                reader.setFullContent(filePath);
            } catch (ReadingException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public View onFragmentReady(int position) {
        reader.setMaxContentPerSection(1250);
        reader.setCssStatus(isPickedWebView ? CssStatus.INCLUDE : CssStatus.OMIT);
        reader.setIsIncludingTextContent(true);

        BookSection bookSection = null;

        try {
            bookSection = reader.readSection(position);
        } catch (ReadingException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (OutOfPagesException e) {
            e.printStackTrace();
            this.pageCount = position;
            mSectionsPagerAdapter.notifyDataSetChanged();
        }

        if (bookSection != null) {
            return setFragmentView(isPickedWebView, bookSection.getSectionContent(), "text/html", "UTF-8"); // reader.isContentStyled
        }

        return null;
    }

    public View setFragmentView(boolean isContentStyled, String data, String mimeType, String encoding) {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (isContentStyled) {
            WebView webView = new WebView(MainActivity.this);
            webView.loadDataWithBaseURL(null, data, mimeType, encoding, null);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            }

            webView.setLayoutParams(layoutParams);

            return webView;
        } else {
            ScrollView scrollView = new ScrollView(MainActivity.this);
            scrollView.setLayoutParams(layoutParams);

            TextView textView = new TextView(MainActivity.this);
            textView.setLayoutParams(layoutParams);

            textView.setText(Html.fromHtml(data, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    String imageAsStr = source.substring(source.indexOf(";base64,") + 8);
                    byte[] imageAsBytes = Base64.decode(imageAsStr, Base64.DEFAULT);
                    Bitmap imageAsBitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                    int[] imageWidthStartEnd = calculateImageStartEndPos(imageAsBitmap.getHeight(), imageAsBitmap.getWidth());

                    Drawable imageAsDrawable = new BitmapDrawable(getResources(), imageAsBitmap);
                    imageAsDrawable.setBounds(imageWidthStartEnd[0], 0, imageWidthStartEnd[1], imageWidthStartEnd[2]);
                    return imageAsDrawable;
                }
            }, null));

            int pxPadding = dpToPx(12);

            textView.setPadding(pxPadding, pxPadding, pxPadding, pxPadding);

            scrollView.addView(textView);
            return scrollView;
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private int[] calculateImageStartEndPos(int bitmapHeight, int bitmapWidth) {

        float imageSizeMultiplier = ((float) pxScreenWidth / bitmapWidth) * 0.6f; // TODO: This is not reasonable. Fix this.

        if (imageSizeMultiplier > 1.0f) {
            bitmapHeight = ((int) (((float) bitmapHeight) * imageSizeMultiplier));
            bitmapWidth = ((int) (((float) bitmapWidth) * imageSizeMultiplier));
        }

        int imageWidthStartPx = (pxScreenWidth - bitmapWidth) / 2;
        int imageWidthEndPx = pxScreenWidth - imageWidthStartPx;

        return new int[]{imageWidthStartPx, imageWidthEndPx, bitmapHeight};
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            return PageFragment.newInstance(position);
        }
    }
}
