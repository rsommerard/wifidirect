package ut.disseminate;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;


public class ImageGridFragment extends Fragment {

    private static final String TAG = ImageGridFragment.class.getSimpleName();

    private static final String ARG_LINKS = "ClickUrls";
    private static final String ARG_IMAGES = "ImageUrls";
    private static final String ARG_LABELS = "TextLabels";
    private static final String ARG_ID = "Id";
    private static final String ARG_HIGHLIGHT = "Highlight";

    private OnImageGridListener mListener;
    DisplayImageOptions options;

    GridView gridView;

    public ArrayList<String> getClickUrls() {
        return clickUrls;
    }

    public void setClickUrls(ArrayList<String> clickUrls) {
        this.clickUrls = clickUrls;
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public ArrayList<String> getTextLabels() {
        return textLabels;
    }

    public void setTextLabels(ArrayList<String> textLabels) {
        this.textLabels = textLabels;
    }

    private ArrayList<String> clickUrls;
    private ArrayList<String> imageUrls;
    private ArrayList<String> textLabels;
    private String id;
    private boolean highlight;

    String[] testImageUrls = new String[] {
            "https://lh6.googleusercontent.com/-55osAWw3x0Q/URquUtcFr5I/AAAAAAAAAbs/rWlj1RUKrYI/s1024/A%252520Photographer.jpg",
            "https://lh4.googleusercontent.com/--dq8niRp7W4/URquVgmXvgI/AAAAAAAAAbs/-gnuLQfNnBA/s1024/A%252520Song%252520of%252520Ice%252520and%252520Fire.jpg",
            "https://lh5.googleusercontent.com/-7qZeDtRKFKc/URquWZT1gOI/AAAAAAAAAbs/hqWgteyNXsg/s1024/Another%252520Rockaway%252520Sunset.jpg",
            "https://lh3.googleusercontent.com/--L0Km39l5J8/URquXHGcdNI/AAAAAAAAAbs/3ZrSJNrSomQ/s1024/Antelope%252520Butte.jpg",
            "https://lh6.googleusercontent.com/-8HO-4vIFnlw/URquZnsFgtI/AAAAAAAAAbs/WT8jViTF7vw/s1024/Antelope%252520Hallway.jpg",
            "https://lh4.googleusercontent.com/-WIuWgVcU3Qw/URqubRVcj4I/AAAAAAAAAbs/YvbwgGjwdIQ/s1024/Antelope%252520Walls.jpg",
            "https://lh6.googleusercontent.com/-UBmLbPELvoQ/URqucCdv0kI/AAAAAAAAAbs/IdNhr2VQoQs/s1024/Apre%2525CC%252580s%252520la%252520Pluie.jpg",
            "https://lh3.googleusercontent.com/-s-AFpvgSeew/URquc6dF-JI/AAAAAAAAAbs/Mt3xNGRUd68/s1024/Backlit%252520Cloud.jpg",
            "https://lh5.googleusercontent.com/-bvmif9a9YOQ/URquea3heHI/AAAAAAAAAbs/rcr6wyeQtAo/s1024/Bee%252520and%252520Flower.jpg",
            "https://lh5.googleusercontent.com/-n7mdm7I7FGs/URqueT_BT-I/AAAAAAAAAbs/9MYmXlmpSAo/s1024/Bonzai%252520Rock%252520Sunset.jpg",
            "https://lh6.googleusercontent.com/-4CN4X4t0M1k/URqufPozWzI/AAAAAAAAAbs/8wK41lg1KPs/s1024/Caterpillar.jpg",
            "https://lh3.googleusercontent.com/-rrFnVC8xQEg/URqufdrLBaI/AAAAAAAAAbs/s69WYy_fl1E/s1024/Chess.jpg",
            "https://lh5.googleusercontent.com/-WVpRptWH8Yw/URqugh-QmDI/AAAAAAAAAbs/E-MgBgtlUWU/s1024/Chihuly.jpg",
            "https://lh5.googleusercontent.com/-0BDXkYmckbo/URquhKFW84I/AAAAAAAAAbs/ogQtHCTk2JQ/s1024/Closed%252520Door.jpg",
            "https://lh3.googleusercontent.com/-PyggXXZRykM/URquh-kVvoI/AAAAAAAAAbs/hFtDwhtrHHQ/s1024/Colorado%252520River%252520Sunset.jpg",
            "https://lh3.googleusercontent.com/-ZAs4dNZtALc/URquikvOCWI/AAAAAAAAAbs/DXz4h3dll1Y/s1024/Colors%252520of%252520Autumn.jpg",
            "https://lh5.googleusercontent.com/-B1HW-z4zwao/URqvFWYRwUI/AAAAAAAAAbs/8Peli53Bs8I/s1024/The%252520Cave%252520BW.jpg",
            "https://lh3.googleusercontent.com/-PO4E-xZKAnQ/URqvGRqjYkI/AAAAAAAAAbs/42nyADFsXag/s1024/The%252520Fisherman.jpg",
            "https://lh4.googleusercontent.com/-iLyZlzfdy7s/URqvG0YScdI/AAAAAAAAAbs/1J9eDKmkXtk/s1024/The%252520Night%252520is%252520Coming.jpg",
            "https://lh6.googleusercontent.com/-G-k7YkkUco0/URqvHhah6fI/AAAAAAAAAbs/_taQQG7t0vo/s1024/The%252520Road.jpg",
            "https://lh6.googleusercontent.com/-h-ALJt7kSus/URqvIThqYfI/AAAAAAAAAbs/ejiv35olWS8/s1024/Tokyo%252520Heights.jpg",
            "https://lh5.googleusercontent.com/-Hy9k-TbS7xg/URqvIjQMOxI/AAAAAAAAAbs/RSpmmOATSkg/s1024/Tokyo%252520Highway.jpg",
            "https://lh6.googleusercontent.com/-83oOvMb4OZs/URqvJL0T7lI/AAAAAAAAAbs/c5TECZ6RONM/s1024/Tokyo%252520Smog.jpg",
            "https://lh3.googleusercontent.com/-FB-jfgREEfI/URqvJI3EXAI/AAAAAAAAAbs/XfyweiRF4v8/s1024/Tufa%252520at%252520Night.jpg",
            "https://lh4.googleusercontent.com/-vngKD5Z1U8w/URqvJUCEgPI/AAAAAAAAAbs/ulxCMVcU6EU/s1024/Valley%252520Sunset.jpg",
            "https://lh6.googleusercontent.com/-DOz5I2E2oMQ/URqvKMND1kI/AAAAAAAAAbs/Iqf0IsInleo/s1024/Windmill%252520Sunrise.jpg",
            "https://lh5.googleusercontent.com/-biyiyWcJ9MU/URqvKculiAI/AAAAAAAAAbs/jyPsCplJOpE/s1024/Windmill.jpg",
            "https://lh4.googleusercontent.com/-PDT167_xRdA/URqvK36mLcI/AAAAAAAAAbs/oi2ik9QseMI/s1024/Windmills.jpg",
            "https://lh5.googleusercontent.com/-kI_QdYx7VlU/URqvLXCB6gI/AAAAAAAAAbs/N31vlZ6u89o/s1024/Yet%252520Another%252520Rockaway%252520Sunset.jpg",
            "https://lh4.googleusercontent.com/-e9NHZ5k5MSs/URqvMIBZjtI/AAAAAAAAAbs/1fV810rDNfQ/s1024/Yosemite%252520Tree.jpg"
    };

    // TODO: Rename and change types and number of parameters
    public static ImageGridFragment newInstance(String id, boolean highlight, ArrayList<String> clickUrls, ArrayList<String> imageUrls, ArrayList<String> textLabels) {
        ImageGridFragment fragment = new ImageGridFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_LINKS, clickUrls);
        args.putStringArrayList(ARG_IMAGES, imageUrls);
        args.putStringArrayList(ARG_LABELS, textLabels);
        args.putString(ARG_ID, id);
        args.putBoolean(ARG_HIGHLIGHT, highlight);
        fragment.setArguments(args);
        return fragment;
    }
    public ImageGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        if (getArguments() != null) {
            clickUrls = getArguments().getStringArrayList(ARG_LINKS);
            imageUrls = getArguments().getStringArrayList(ARG_IMAGES);
            textLabels = getArguments().getStringArrayList(ARG_LABELS);
            id = getArguments().getString(ARG_ID);
            highlight = getArguments().getBoolean(ARG_HIGHLIGHT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_image_grid, container, false);
        if (imageUrls == null) {
            return rootView;
        }

        GridView gridview = (GridView) rootView.findViewById(R.id.grid);
        gridview.setAdapter(new ImageAdapter());
        if (highlight) {
            gridview.setBackgroundColor(Color.parseColor("#607D8B"));
        }
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Log.d(TAG, "Position: " + position);
                //view.setBackgroundColor(Color.parseColor("#e59a0f"));
                //adapterView.setBackgroundColor(Color.parseColor("#607D8B"));
                mListener.imageClickHandler(ImageGridFragment.this.id);
            }
        });
        return rootView;
    }

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        ImageAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.image_grid_adapter, parent, false);
                holder = new ViewHolder();

                holder.imageView = (ImageView) view.findViewById(R.id.image);
                //holder.textLabel = (TextView) view.findViewById(R.id.item_text);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ImageDownloader.Scheme scheme = ImageDownloader.Scheme.ofUri(imageUrls.get(position));
            if (scheme != ImageDownloader.Scheme.UNKNOWN) {
                ImageLoader.getInstance().displayImage(imageUrls.get(position), holder.imageView, options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Log.d(TAG, "Could not load image");
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        //Log.d(TAG, "Image successfully loaded");
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
            }
            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnImageGridListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnImageGridListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnImageGridListener {
        public void imageClickHandler(String itemId);
    }

    public void initImageLoader() {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.

        // Initialize ImageLoader with configuration.
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(getActivity().getApplicationContext());
            ImageLoader.getInstance().init(config);
        }
    }
}
