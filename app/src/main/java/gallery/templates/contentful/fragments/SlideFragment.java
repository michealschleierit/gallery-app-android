package gallery.templates.contentful.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import butterknife.ButterKnife;
import butterknife.BindView;
import gallery.templates.contentful.R;
import gallery.templates.contentful.lib.Const;
import gallery.templates.contentful.lib.Intents;
import gallery.templates.contentful.lib.TargetAdapter;
import gallery.templates.contentful.lib.Utils;
import gallery.templates.contentful.ui.ViewUtils;
import gallery.templates.contentful.vault.Image;

public class SlideFragment extends Fragment implements Palette.PaletteAsyncListener, View.OnClickListener{
  private Image image;

  private AsyncTask paletteTask;

  private Target target;

  private Bitmap bitmap;
  private Bitmap bitmap_fitted;

  private boolean hasPalette;

  private int colorLightMuted;

  private int colorDarkMuted;

  private int colorVibrant;

  Context context;

  @BindView(R.id.photo) ImageView photo;

  @BindView(R.id.photo_fitted) ImageView photo_fitted;

  @BindView(R.id.bottom) ViewGroup bottomContainer;

  @BindView(R.id.title) TextView title;

  @BindView(R.id.caption) TextView caption;

  public static SlideFragment newSlide(Context context, Image image) {
    Bundle b = new Bundle();
    b.putParcelable(Intents.EXTRA_IMAGE, Parcels.wrap(image));
    return (SlideFragment) SlideFragment.instantiate(context, SlideFragment.class.getName(), b);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    extractIntentArguments();
    displayPhoto(true);
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                     @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.fragment_slide, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);

    ViewUtils.setViewHeight(photo, Const.IMAGE_HEIGHT, true);
    title.setText(image.title());
    caption.setText(image.caption());

    title.setOnClickListener(this);
    caption.setOnClickListener(this);

    photo_fitted.setVisibility(Const.DISPLAY_FITTED_IMAGE ? View.VISIBLE : View.GONE);
    photo.setVisibility(!Const.DISPLAY_FITTED_IMAGE ? View.VISIBLE : View.GONE);

    applyColor();
    applyImage(false);
  }

  @Override
  public void onClick(View v) {
    context = this.getActivity();

    switch (v.getId()){
      case R.id.title:
      case R.id.caption:

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(image.photo().url(), image.photo().url());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "URL COPIED TO CLICKBOARD", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  public void onChangeImageSize() {
//    photo_fitted.setVisibility(Const.DISPLAY_FITTED_IMAGE ? View.VISIBLE : View.GONE);
//    photo.setVisibility(!Const.DISPLAY_FITTED_IMAGE ? View.VISIBLE : View.GONE);
  }

  @Override public void onDestroy() {
    cancelPaletteTask();
    if (target != null) {
      Picasso.get().cancelRequest(target);
      target = null;
    }

    bitmap = null;
    super.onDestroy();
  }

  @Override public void onGenerated(Palette palette) {
    hasPalette = true;
    target = null;
    int black = getResources().getColor(android.R.color.black);
    int white = getResources().getColor(android.R.color.white);
    colorDarkMuted = palette.getDarkMutedColor(black);
    colorVibrant = palette.getVibrantColor(white);
    colorLightMuted = palette.getLightMutedColor(white);
    applyColor();
    sendPalette();
  }

  private void extractIntentArguments() {
    Bundle b = getArguments();
    image = Parcels.unwrap(b.getParcelable(Intents.EXTRA_IMAGE));
  }

  private void cancelPaletteTask() {
    if (paletteTask != null) {
      paletteTask.cancel(true);
      paletteTask = null;
    }
  }

  private void sendPalette() {
    getActivity().sendBroadcast(attachColors(
            new Intent(Intents.ACTION_COLORIZE)
                    .putExtra(Intents.EXTRA_IMAGE, Parcels.wrap(image))));
  }

  private Intent attachColors(Intent intent) {
    intent.putExtra(Intents.EXTRA_CLR_LIGHT_MUTED, colorLightMuted);
    intent.putExtra(Intents.EXTRA_CLR_DARK_MUTED, colorDarkMuted);
    intent.putExtra(Intents.EXTRA_CLR_VIBRANT, colorVibrant);
    return intent;
  }

  private void applyImage(Boolean is_fitted_image) {

    if (bitmap != null && photo != null && !is_fitted_image) {
      photo.setImageBitmap(bitmap);
    }

    if (is_fitted_image && bitmap_fitted != null && photo_fitted != null) {
      photo_fitted.setImageBitmap(bitmap_fitted);
    }
  }

  private void applyColor() {
    if (hasPalette && bottomContainer != null) {
      bottomContainer.setBackgroundColor(colorDarkMuted);
      title.setTextColor(colorVibrant);
      caption.setTextColor(colorLightMuted);
      bottomContainer.animate().alpha(1.0f).setDuration(200).start();
      sendPalette();
    }
  }

  private void displayPhoto(Boolean centerCrop) {
    Picasso.get()
            .load(Utils.imageUrl(image.photo().url()))
            .resize(Const.IMAGE_WIDTH, Const.IMAGE_HEIGHT)
            .centerCrop()
            .into(target = new TargetAdapter() {
              @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                SlideFragment.this.bitmap = bitmap;
                applyImage(false);
                paletteTask = new Palette.Builder(bitmap)
                        .maximumColorCount(32)
                        .generate(SlideFragment.this);
              }
            });

    Picasso.get()
            .load(Utils.imageUrl(image.photo().url()))
            .into(target = new TargetAdapter() {
              @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                SlideFragment.this.bitmap_fitted = bitmap;
                applyImage(true);
              }
            });
  }

  public int getColorVibrant() {
    return colorVibrant;
  }

  public int getColorDarkMuted() {
    return colorDarkMuted;
  }

  public int getColorLightMuted() {
    return colorLightMuted;
  }
}
