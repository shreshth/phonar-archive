package com.phonar.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.phonar.ContactsList;
import com.phonar.R;
import com.phonar.models.Contact;

public class ContactsListAdapter extends BaseAdapter {

    // need to have phonared friend this many times to be considered favorite
    private Context mContext;

    public static final String KEY_NONE = "none";
    public static final String KEY_LOADING = "loading";

    // map from i to id
    public Map<Integer, String> map;

    public ContactsListAdapter(Context context, ListView listView) {
        this.mContext = context;
        this.formMap();
    }

    @Override
    public int getCount() {
        return map.size();
    }

    @Override
    public void notifyDataSetChanged() {
        this.formMap();
        super.notifyDataSetChanged();
    }

    private void formMap() {
        map = new HashMap<Integer, String>();
        boolean all_added = false;
        boolean loading_added = false;
        int map_index = 0;
        List<Contact> friends = ContactsList.getAllContacts(mContext);
        // add all contacts to list
        for (Contact friend : friends) {
            if (searchMatches(friend.name)) {
                all_added = true;
                map.put(map_index, friend.lookupKey);
                map_index++;
            }
        }
        // add loading indicator if have to
        if (ContactsList.mLoading) {
            map.put(map_index, KEY_LOADING);
            map_index++;
            loading_added = true;
        }
        // add no contacts label if have to
        if (!(all_added || loading_added)) {
            map.put(map_index, KEY_NONE);
            map_index++;
        }
    }

    @Override
    public Object getItem(int i) {
        return map.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View row, ViewGroup root) {
        String id = map.get(i);
        LayoutInflater inflater =
            (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (id.equals(KEY_NONE)) {
            row = inflater.inflate(R.layout.request_divider_none, root, false);
        } else if (id.equals(KEY_LOADING)) {
            row = inflater.inflate(R.layout.request_divider_loading, root, false);
        } else {
            row = inflater.inflate(R.layout.device, root, false);
            Contact friend = ContactsList.getFriend(mContext, id);
            ((ImageView) ((ViewGroup) row).getChildAt(0)).setImageBitmap(friend.image);
            // highlight part of name if search term matches
            if (ContactPickerActivity.mSearchString.isEmpty() || !searchMatches(friend.name)) {
                ((TextView) ((ViewGroup) ((ViewGroup) row).getChildAt(1)).getChildAt(0))
                    .setText(friend.name);
            } else {
                String html =
                    "<b>"
                        + friend.name.substring(0, ContactPickerActivity.mSearchString.length())
                        + "</b>"
                        + friend.name.substring(ContactPickerActivity.mSearchString.length());
                ((TextView) ((ViewGroup) ((ViewGroup) row).getChildAt(1)).getChildAt(0))
                    .setText(Html.fromHtml(html));
            }
            ((ToggleButton) ((ViewGroup) row).getChildAt(3)).setVisibility(View.GONE);

            // time of last location text
            ((TextView) ((ViewGroup) ((ViewGroup) row).getChildAt(1)).getChildAt(1))
                .setVisibility(View.GONE);

            // error icon
            ((ViewGroup) row).getChildAt(4).setVisibility(View.GONE);

            // progress spinny wheel
            ((ViewGroup) row).getChildAt(2).setVisibility(View.GONE);
        }

        return row;
    }

    private static boolean searchMatches(String name) {
        if (ContactPickerActivity.mSearchString.isEmpty()) {
            return true;
        } else if (ContactPickerActivity.mSearchString.length() > name.length()) {
            return false;
        } else {
            return name
                .toLowerCase().substring(0, ContactPickerActivity.mSearchString.length())
                .equals(ContactPickerActivity.mSearchString.toLowerCase());
        }
    }
}
