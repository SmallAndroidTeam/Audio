package com.of.music.fragment.fragmentSettings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.of.music.R;
import com.of.music.activity.MainActivity;
import com.of.music.fragment.AttachDialogFragment;


public class SourcequalityFragment extends AttachDialogFragment {
    private TextView selectTextview,standardTextView,hightqualityTextView,selectTextviewwifi,standardTextViewwifi,hightqualityTextViewwifi;
    private ImageView selectImageView,standardImageView,highqualityImageView,returnimageview,selectImageViewwifi,standardImageViewwifi,highqualityImageViewwifi;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.activity_sourcequality, container);
        selectTextview=view.findViewById(R.id.select_textview);
        selectImageView=view.findViewById(R.id.select_imageview);
        standardTextView=view.findViewById(R.id.standard_textview);
        standardImageView=view.findViewById(R.id.standard_imageview);
        hightqualityTextView=view.findViewById(R.id.highquality_textview);
        highqualityImageView=view.findViewById(R.id.highquality_imageview);
        returnimageview=view.findViewById(R.id.return_imageview);
    
        selectTextviewwifi=view.findViewById(R.id.select_textviewwifi);
        selectImageViewwifi=view.findViewById(R.id.select_imageviewwifi);
        standardTextViewwifi=view.findViewById(R.id.standard_textviewwifi);
        standardImageViewwifi=view.findViewById(R.id.standard_imageviewwifi);
        hightqualityTextViewwifi=view.findViewById(R.id.highquality_textviewwifi);
        highqualityImageViewwifi=view.findViewById(R.id.highquality_imageviewwifi);
        
        
        selectTextview.setOnClickListener(selectClick);
        standardTextView.setOnClickListener(standardClick);
        hightqualityTextView.setOnClickListener(highqualityClick);
        selectTextviewwifi.setOnClickListener(selectClickwifi);
        standardTextViewwifi.setOnClickListener(standardClickwifi);
        hightqualityTextViewwifi.setOnClickListener(highqualityClickwifi);
        returnimageview.setOnClickListener(returnimageviewClick);
        return view;
    }
    View.OnClickListener selectClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImageView.setVisibility(View.VISIBLE);
            standardImageView.setVisibility(View.INVISIBLE);
            highqualityImageView.setVisibility(View.INVISIBLE);
        }
    };
    View.OnClickListener standardClick =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImageView.setVisibility(View.INVISIBLE);
            standardImageView.setVisibility(View.VISIBLE);
            highqualityImageView.setVisibility(View.INVISIBLE);
        }
    };
    View.OnClickListener highqualityClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImageView.setVisibility(View.INVISIBLE);
            standardImageView.setVisibility(View.INVISIBLE);
            highqualityImageView.setVisibility(View.VISIBLE);
        }
    };
    View.OnClickListener selectClickwifi=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImageViewwifi.setVisibility(View.VISIBLE);
            standardImageViewwifi.setVisibility(View.INVISIBLE);
            highqualityImageViewwifi.setVisibility(View.INVISIBLE);
        }
    };
    View.OnClickListener standardClickwifi =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImageViewwifi.setVisibility(View.INVISIBLE);
            standardImageViewwifi.setVisibility(View.VISIBLE);
            highqualityImageViewwifi.setVisibility(View.INVISIBLE);
        }
    };
    View.OnClickListener highqualityClickwifi=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImageViewwifi.setVisibility(View.INVISIBLE);
            standardImageViewwifi.setVisibility(View.INVISIBLE);
            highqualityImageViewwifi.setVisibility(View.VISIBLE);
        }
    };
    View.OnClickListener returnimageviewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v){
           Intent intent=new Intent(getActivity(),MainActivity.class);
           startActivity(intent);
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置样式
        //setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        //设置fragment高度 、宽度
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels );
        int dialogWidth = (int) (mContext.getResources().getDisplayMetrics().widthPixels );
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);
        
    }
    
}
