package dubs.queueitup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import dubs.queueitup.Models.Party;

/**
 * Created by ryanschott on 2017-10-30.
 */

public class PartyDetailsPage extends Fragment {

    View view;
    TextView pname;
    TextView jcode;
    Party party_details;

    public PartyDetailsPage() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.party_details_page, container, false);

        pname = (TextView) view.findViewById(R.id.party_name);
        jcode = (TextView) view.findViewById(R.id.join_code);

        if (savedInstanceState != null && party_details == null) {
            party_details = (Party) savedInstanceState.getParcelable("party");
        }

        if (party_details != null) {
            pname.setText(party_details.getName());
            jcode.setText(party_details.getCode());
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            party_details = (Party) getArguments().getParcelable("party");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("party", party_details);
    }
}
