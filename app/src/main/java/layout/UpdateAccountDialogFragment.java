package layout;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.SwitchCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iuxta.nearby.AppUtils;
import iuxta.nearby.DobPickerFragment;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.SharedAsyncMethods;
import iuxta.nearby.model.User;

import static android.content.Context.WIFI_SERVICE;


public class UpdateAccountDialogFragment extends DialogFragment {
    private Context context;
    private User user;
    private TextInputLayout firstNameLayout;
    private EditText firstName;
    private TextInputLayout lastNameLayout;
    private EditText lastName;
    private TextInputLayout addressLine1Layout;
    private EditText addressLine1;
    private EditText addressLine2;
    private TextInputLayout cityLayout;
    private EditText city;
    private TextInputLayout stateLayout;
    private EditText state;
    private TextInputLayout zipLayout;
    private EditText zip;
    private TextInputLayout emailLayout;
    private EditText email;
    private TextInputLayout phoneLayout;
    private EditText phone;
    private SwitchCompat notificationsNearHome;
    private SwitchCompat notificationsNearby;
    private Double currentRadius;
    private OnFragmentInteractionListener mListener;
    private View view;
    private TextInputLayout dobLayout;
    private EditText dob;
    private ScrollView screen1;
    private ScrollView screen2;
    private RelativeLayout updatingScreen;
    private Map<Double, String> radiusMap = new HashMap<Double, String>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccountFragment.
     */
    public static UpdateAccountDialogFragment newInstance() {
        UpdateAccountDialogFragment fragment = new UpdateAccountDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // request a window without the title
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_update_account_dialog, container, false);
        screen1 = (ScrollView) view.findViewById(R.id.account_1);
        screen2 = (ScrollView) view.findViewById(R.id.account_2);
        updatingScreen = (RelativeLayout) view.findViewById(R.id.updating_account_screen);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_edit_profile);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        firstNameLayout = (TextInputLayout) view.findViewById(R.id.first_name_layout);
        firstName = (EditText) view.findViewById(R.id.first_name);
        Drawable drawable = firstName.getBackground().getConstantState().newDrawable();
        firstName.setBackgroundDrawable(drawable);
        firstName.setText(user.getFirstName());
        lastNameLayout = (TextInputLayout) view.findViewById(R.id.last_name_layout);
        lastName = (EditText) view.findViewById(R.id.last_name);
        lastName.setText(user.getLastName());
        addressLine1Layout = (TextInputLayout) view.findViewById(R.id.address_line1_layout);
        addressLine1 = (EditText) view.findViewById(R.id.address_line1);
        addressLine1.setText(user.getAddress());
        addressLine2 = (EditText) view.findViewById(R.id.address_line2);
        addressLine2.setText(user.getAddressLine2());
        cityLayout = (TextInputLayout) view.findViewById(R.id.city_layout);
        city = (EditText) view.findViewById(R.id.city);
        city.setText(user.getCity());
        stateLayout = (TextInputLayout) view.findViewById(R.id.state_layout);
        state = (EditText) view.findViewById(R.id.state);
        state.setText(user.getState());
        zipLayout = (TextInputLayout) view.findViewById(R.id.zipcode_layout);
        zip = (EditText) view.findViewById(R.id.zipcode);
        zip.setText(user.getZip());
        emailLayout = (TextInputLayout) view.findViewById(R.id.email_layout);
        email = (EditText) view.findViewById(R.id.email);
        email.setText(user.getEmail());
        phoneLayout = (TextInputLayout) view.findViewById(R.id.phone_layout);
        phone = (EditText) view.findViewById(R.id.phone);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        phone.setText(user.getPhone());

        dobLayout = (TextInputLayout) view.findViewById(R.id.dob_layout);
        dob = (EditText) view.findViewById(R.id.dob);
        dob.setText(user.getDateOfBirth());
        Log.i("UpdateAccount", "dob from user data = " + user.getDateOfBirth());

        notificationsNearHome = (SwitchCompat) view.findViewById(R.id.notifications_near_home);
        if (user.getHomeLocationNotifications() != null) {
            notificationsNearHome.setChecked(user.getHomeLocationNotifications());
        } else {
            notificationsNearHome.setChecked(false);
        }

        notificationsNearby = (SwitchCompat) view.findViewById(R.id.notifications_nearby);
        if (user.getCurrentLocationNotifications() != null) {
            notificationsNearby.setChecked(user.getCurrentLocationNotifications());
        } else {
            notificationsNearby.setChecked(false);
        }
        configureRadiusSpinner(view);

        final Button nextBtn1 = (Button) view.findViewById(R.id.next_button_1);
        nextBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextBtn1.setEnabled(false);
                if (validateScreenOne()) {
                    screen1.setVisibility(View.GONE);
                    screen2.setVisibility(View.VISIBLE);
                }
                nextBtn1.setEnabled(true);

            }
        });

        ImageButton backMiddle = (ImageButton) view.findViewById(R.id.back_middle);
        backMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screen2.setVisibility(View.GONE);
                screen1.setVisibility(View.VISIBLE);
            }
        });

        dob.setKeyListener(null);
        dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    showDobPickerDialog();
                } else {
                    // Hide your calender here
                }
            }
        });
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDobPickerDialog();
            }
        });



        final Button saveBtn = (Button) view.findViewById(R.id.next_button_2);
        saveBtn.setText("save");
        setSaveBtnClick(saveBtn);
         this.view = view;
        return view;
    }

    private void setSaveBtnClick(Button saveBtn) {
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateScreenTwo()) {
                    return;
                }
                screen2.setVisibility(View.GONE);
                updatingScreen.setVisibility(View.VISIBLE);
                String first = firstName.getText().toString();
                user.setFirstName(first);
                String last = lastName.getText().toString();
                user.setLastName(last);
                String address1 = addressLine1.getText().toString();
                user.setAddress(AppUtils.validateString(address1) ? address1 : null);
                String address2 = addressLine2.getText().toString();
                user.setAddressLine2(AppUtils.validateString(address2) ? address2 : null);
                String sCity = city.getText().toString();
                user.setCity(AppUtils.validateString(sCity) ? sCity : null);
                String sState = state.getText().toString();
                user.setState(AppUtils.validateString(sState) ? sState : null);
                String sZip = zip.getText().toString();
                user.setZip(AppUtils.validateString(sZip) ? sZip : null);
                user.setCurrentLocationNotifications(notificationsNearby.isChecked());
                user.setHomeLocationNotifications(notificationsNearHome.isChecked());
                user.setNewRequestNotificationsEnabled(notificationsNearby.isChecked() || notificationsNearHome.isChecked());
                user.setNotificationRadius(currentRadius);
                String sEmail = email.getText().toString();
                user.setEmail(AppUtils.validateString(sEmail) ? sEmail : null);
                String sPhone = phone.getText().toString();
                user.setDateOfBirth(dob.getText().toString());
                if (user.getTosAccepted() == null || !user.getTosAccepted()) {
                    user.setTosAccepted(true);
                    WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    user.setTosAcceptIp(ip);
                }
                if (sPhone != null) {
                    sPhone = sPhone.replaceAll("\\)", "");
                    sPhone = sPhone.replaceAll("\\(", "");
                    sPhone = sPhone.replaceAll(" ", "-");
                }
                user.setPhone(AppUtils.validateString(sPhone) ? sPhone : null);
                SharedAsyncMethods.updateUser(user, context, (MainActivity) getActivity(), view);
            }
        });
    }

    private boolean validateScreenOne() {
        boolean valid = true;
        String first = firstName.getText().toString();
        if (first.isEmpty() || first.length() < 2 || first.trim().length() < 2) {
            firstNameLayout.setError("please enter a first name that is at least 2 characters long");
            valid = false;
        } else {
            firstNameLayout.setError(null);
        }
        String last = lastName.getText().toString();
        if (last.isEmpty() || last.length() < 2 || last.trim().length() < 2) {
            lastNameLayout.setError("please enter a last name that is at least 2 characters long");
            valid = false;
        } else {
            lastNameLayout.setError(null);
        }
        String address = addressLine1.getText().toString();
        if (address.isEmpty() || address.length() < 6 || address.trim().length() < 6) {
            addressLine1Layout.setError("please enter a valid address");
            valid = false;
        } else {
            addressLine1Layout.setError(null);
        }
        String cityText = city.getText().toString();
        if (cityText.isEmpty() || cityText.length() < 2 || cityText.trim().length() < 2) {
            cityLayout.setError("please enter a valid city");
            valid = false;
        } else {
            cityLayout.setError(null);
        }
        String stateText = state.getText().toString();
        if (stateText.isEmpty() || stateText.length() != 2 || stateText.trim().length() < 2) {
            stateLayout.setError("please enter your state");
            valid = false;
        } else {
            stateLayout.setError(null);
        }
        String zipString = zip.getText().toString();
        if (zipString.isEmpty() || zipString.length() != 5 || zipString.trim().length() < 5) {
            zipLayout.setError("please enter your 5 digit zip code");
            valid = false;
        } else {
            zipLayout.setError(null);
        }
        return valid;
    }

    private boolean validateScreenTwo() {
        boolean valid = true;
        String emailString = email.getText().toString();
        if (emailString.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            emailLayout.setError("please enter a valid email");
            valid = false;
        } else {
            emailLayout.setError(null);
        }
        String phoneString = phone.getText().toString();
        if (phoneString.isEmpty() || !Patterns.PHONE.matcher(phoneString).matches()) {
            phoneLayout.setError("please enter a valid phone number");
            valid = false;
        } else {
            phoneLayout.setError(null);
        }
        String dobString = dob.getText().toString();
        if (dobString.isEmpty()) {
            dobLayout.setError("please enter your date of birth");
            valid = false;
        } else {
            dobLayout.setError(null);
        }
        return valid;
    }


    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            month += 1;
            dob.setText(String.format("%1$04d-%2$02d-%3$02d", year, month, day));
            Log.i("OnDateSet ", "DOB String = " + dob.getText().toString());
        }
    };

    private void showDobPickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = 1995;
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (user.getDateOfBirth() != null) {
            String[] dobArray = user.getDateOfBirth().split("-");
            if (dobArray[0] != null) {
                year = Integer.parseInt(dobArray[0]);
            }
            if (dobArray[1] != null) {
                month = Integer.parseInt(dobArray[1]) - 1;
            }
            if (dobArray[2] != null) {
                day = Integer.parseInt(dobArray[2]);
            }
        }

        DobPickerFragment dobPicker = new DobPickerFragment(context, android.R.style.Theme_Holo_Light_Dialog, datePickerListener, year, month, day);
        DatePickerDialog dialog = dobPicker.getPicker();
        dialog.show();
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            //TODO: This doesn't work!!
            dialog.getWindow().setWindowAnimations(R.style.RequestDialog);
        }
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
        mListener = (OnFragmentInteractionListener) activity;

    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

    private List<Double> getRadiusList() {
        List<Double> radiusList = new ArrayList<>();
        radiusList.add(.1);
        radiusList.add(.25);
        radiusList.add(.5);
        radiusList.add(1D);
        radiusList.add(5D);
        radiusList.add(10D);
        return radiusList;
    }

    private void configureRadiusSpinner(View v) {
        Spinner radiusSpinner = (Spinner) v.findViewById(R.id.radius_spinner);
        List<String> radiusList = new ArrayList<>();
        radiusMap.put(.1, ".1 miles");
        radiusMap.put(.25, ".25 miles");
        radiusMap.put(.5, ".5 miles ");
        radiusMap.put(1.0, "1 mile");
        radiusMap.put(5.0, "5 miles");
        radiusMap.put(10.0, "10 miles");
        radiusList.add(radiusMap.get(.1));
        radiusList.add(radiusMap.get(.25));
        radiusList.add(radiusMap.get(.5));
        radiusList.add(radiusMap.get(1.0));
        radiusList.add(radiusMap.get(5.0));
        radiusList.add(radiusMap.get(10.0));
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, R.layout.simple_spinner_item, radiusList);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);

        // attaching data adapter to spinner
        radiusSpinner.setAdapter(dataAdapter);

        // set radius spinner to current radius
        if (user.getNotificationRadius() != null) {
            String selectedRadius = radiusMap.get(user.getNotificationRadius());
            for (int i = 0; i < radiusList.size(); i++) {
                if (radiusList.get(i).equals(selectedRadius)) {
                    radiusSpinner.setSelection(i+1);
                    break;
                }
            }
        } else {
            radiusSpinner.setSelection(4);
        }

        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                String radiusString = (String) parentView.getItemAtPosition(position);
                for (Map.Entry<Double, String> entry : radiusMap.entrySet()) {
                    Double key = entry.getKey();
                    String value = entry.getValue();
                    if (value.equals(radiusString)) {
                        currentRadius = key;
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }
}