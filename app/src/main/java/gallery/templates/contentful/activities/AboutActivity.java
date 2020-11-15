package gallery.templates.contentful.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
//import butterknife.OnClick;
import gallery.templates.contentful.R;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener{
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
//    ButterKnife.bind(this);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @SuppressWarnings("unused")
  @Override
  public void onClick(View v) {
    int urlResId;
    switch (v.getId()){
      case R.id.btn_faq:
        urlResId = R.string.url_faq;
        break;

      case R.id.btn_feedback:
        urlResId = R.string.url_feedback;
        break;

      case R.id.btn_contact:
        urlResId = R.string.url_contact;
        break;

      case R.id.btn_license:
        urlResId = R.string.url_licensing;
        break;

      default:
        return;
    }

    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(urlResId))));
  }

}
