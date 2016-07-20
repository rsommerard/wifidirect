package ut.disseminate;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageFragment extends Fragment {

    private static final String TAG = ImageFragment.class.getSimpleName();

    private static final String IMAGE_CONTENTS = "image_contents";

    byte[] imageContents;

    private OnImageListener mListener;

    public static ImageFragment newInstance(byte[] imageContents) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putByteArray(IMAGE_CONTENTS, imageContents);
        fragment.setArguments(args);
        return fragment;
    }
    public ImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageContents = getArguments().getByteArray(IMAGE_CONTENTS);
        }
    }

    //Function that converts the byte array into a bitmap
    void setImage(byte[] imageData, ImageView viewer){
        byte[] data = imageData;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        viewer.setImageBitmap(bmp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ImageView view = (ImageView) inflater.inflate(R.layout.fragment_image_view, container, false);
        setImage(imageContents, view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnImageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnStreamButtonsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    public interface OnImageListener {
        //public void cancelButtonHandler();
        //public void downloadButtonHandler();
    }

}
