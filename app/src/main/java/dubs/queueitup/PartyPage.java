package dubs.queueitup;

/**
 * Created by ryanschott on 2017-09-28.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PartyPage extends Fragment implements View.OnClickListener{
    Button createPartyButton;
    public static final String ARG_TITLE = "arg_title";

    public PartyPage() {
    // Required empty public constructor
    }

    OnCreatePartyButtonListener mListener;

    public interface OnCreatePartyButtonListener {
        void onCreateParty(String password);
    }

    public void onClick(View view){
        if (mListener != null) {
            mListener.onCreateParty(String.valueOf("testpw"));
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.party_page, container, false);
        createPartyButton = (Button) view.findViewById(R.id.createPartyButton);

        createPartyButton.setOnClickListener(this);

        return view;
    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState){
//        createPartyButton.setOnClickListener(OnCreatePartyButtonListener(){
//            @Override
//            public void onClick(View view) {
//                Log.d("PartyPage", "Create party clicked");
//                MainActivity.myBundle.putString("party_pw", "testpw");
//            }
//        });
//    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof OnCreatePartyButtonListener) {
            mListener = (OnCreatePartyButtonListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCategoryFragmentListener");
        }
    }

    public static PartyPage newInstance(String text) {

        PartyPage f = new PartyPage();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

}
