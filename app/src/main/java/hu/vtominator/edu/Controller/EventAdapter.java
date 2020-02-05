package hu.vtominator.edu.Controller;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.vtominator.edu.Model.Constants;
import hu.vtominator.edu.Model.Event;
import hu.vtominator.edu.R;

import static android.view.View.GONE;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> implements Filterable {
    private static final String TAG = "EventAdapter";
    private List<Event> eventList;
    private List<Event> eventListFull;


    private OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onPinnedClick(int position);

        void onDeleteClick(int position);

        void onCommentClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public static class EventViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFavorite, imgComment, imgPin, imgDelete, eventPicture;
        private TextView tvEventName, tvEventDate, tvEventTime, tvLocation, tvOrganizer, tvEventDescription, tvLikes, tvComments;


        public EventViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvOrganizer = itemView.findViewById(R.id.tvOrganizer);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);

            eventPicture = itemView.findViewById(R.id.eventPicture);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            imgComment = itemView.findViewById(R.id.imgComment);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);

            imgPin = itemView.findViewById(R.id.imgPin);
            imgDelete = itemView.findViewById(R.id.imgDelete);

            imgPin.setVisibility(GONE);
            imgDelete.setVisibility(GONE);

            final Context mContext = tvEventName.getContext();
            final String currentUserName = SharedPrefManager.getInstance(mContext).getUsername();
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);

            if (account == null && currentUserName.equals("admin")) {

                imgPin.setVisibility(View.VISIBLE);
                imgDelete.setVisibility(View.VISIBLE);

                imgPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            listener.onPinnedClick(position);
                        }
                    }
                });

                imgDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            listener.onDeleteClick(position);
                        }
                    }
                });

            }
            imgComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        listener.onCommentClick(position);
                    }
                }
            });

        }
    }

    public EventAdapter(List<Event> eventList) {
        eventListFull = new ArrayList<>(eventList);
        this.eventList = eventList;

    }

    @Override
    public int getItemViewType(int position) {
        Event currentEvent = eventList.get(position);
        return currentEvent.getPinned();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_home_list_event, viewGroup, false);
        EventViewHolder eventViewHolder = new EventViewHolder(view, mListener);

        if (i == 1) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_pinned_home_list_event, viewGroup, false);
            eventViewHolder = new EventViewHolder(view, mListener);
        }

        return eventViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder viewHolder, int i) {
        final Event currentEvent = eventList.get(i);


        viewHolder.tvEventName.setText(currentEvent.getEventname());
        viewHolder.tvEventDate.setText(currentEvent.getDate());
        viewHolder.tvEventTime.setText(currentEvent.getTime().substring(0, 5));
        viewHolder.tvLocation.setText(currentEvent.getLocation());
        viewHolder.tvOrganizer.setText(currentEvent.getOrganiser());
        viewHolder.tvEventDescription.setText(currentEvent.getDescription());


        if (currentEvent.getPinned() == 1) {
            viewHolder.imgPin.setVisibility(GONE);
            String url = Constants.ROOT_URL + currentEvent.getPicture();
            Picasso.get().load(url).into(viewHolder.eventPicture);
        }

        getFavorites(viewHolder, currentEvent);
        getLikes(viewHolder, currentEvent);
        getComments(viewHolder, currentEvent);


        final Event tempEvent = currentEvent;

        viewHolder.imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentEvent.isFavorite()) addFavorite(v, tempEvent);
                else deleteFavorite(v, tempEvent);
            }
        });


    }


    private void addFavorite(final View v, final Event currentEvent) {

        final String user_id = SharedPrefManager.getInstance(v.getContext()).getUserId();
        final int event_id = currentEvent.getEvent_id();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_SETFAVORITE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Toast.makeText(v.getContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    if (!jsonObject.getBoolean("error")) {

                        currentEvent.setFavorite(true);
                        notifyDataSetChanged();


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(v.getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", user_id);
                params.put("event_id", Integer.toString(event_id));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(v.getContext());
        requestQueue.add(stringRequest);
    }

    private void deleteFavorite(final View v, final Event currentEvent) {
        final String user_id = SharedPrefManager.getInstance(v.getContext()).getUserId();
        final int event_id = currentEvent.getEvent_id();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_DELETEFAVORITE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Toast.makeText(v.getContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    if (!jsonObject.getBoolean("error")) {

                        currentEvent.setFavorite(false);
                        notifyDataSetChanged();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(v.getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", user_id);
                params.put("event_id", Integer.toString(event_id));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(v.getContext());
        requestQueue.add(stringRequest);
    }

    private void getFavorites(final EventViewHolder viewHolder, final Event currentEvent) {
        final Context mContext = viewHolder.tvEventName.getContext();

        final String currentUserId = SharedPrefManager.getInstance(mContext).getUserId();
        final int currentEventId = currentEvent.getEvent_id();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.URL_GETFAVORITES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean("error")) {
                        JSONArray favorites = object.getJSONArray("result");

                        for (int i = 0; i < favorites.length(); i++) {
                            JSONObject favObj = favorites.getJSONObject(i);

                            String user_id = favObj.getString("user_id");
                            int event_id = favObj.getInt("event_id");


                            if (currentUserId == null) {
                                if (account == null) {
                                    viewHolder.imgFavorite.setClickable(false);
                                }
                            } else if (currentUserId.equals(user_id)) {

                                if (currentEventId == event_id) {
                                    viewHolder.imgFavorite.setBackgroundResource(R.drawable.ic_favorite);
                                    currentEvent.setFavorite(true);
                                    return;
                                }
                            }
                            viewHolder.imgFavorite.setBackgroundResource(R.drawable.ic_nofavorite);
                        }
                    } else {
                        viewHolder.imgFavorite.setBackgroundResource(R.drawable.ic_nofavorite);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }

    private void getLikes(final EventViewHolder viewHolder, final Event currentEvent) {
        final Context mContext = viewHolder.tvLikes.getContext();
        final String currentUserName = SharedPrefManager.getInstance(mContext).getUsername();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);

        final int event_id = currentEvent.getEvent_id();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_GETLIKES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject likeObject = new JSONObject(response);

                    if (!likeObject.getBoolean("error")) {
                        JSONArray jsonArray = likeObject.getJSONArray("likes");
                        JSONObject o = jsonArray.getJSONObject(0);

                        int like = o.getInt("likes");


                        currentEvent.setLike(like);
                        if (account == null && currentUserName.equals("Vendég"))
                            viewHolder.tvLikes.setText("A kedveléshez be kell jelentkeznie!");
                        else
                            viewHolder.tvLikes.setText(Integer.toString(currentEvent.getLike()) + " embernek tetszik");

                    } else {
                        if (account == null && currentUserName.equals("Vendég"))
                            viewHolder.tvLikes.setText("A kedveléshez be kell jelentkeznie!");
                        else viewHolder.tvLikes.setText(Integer.toString(0) + " embernek tetszik");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("event_id", Integer.toString(event_id));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }

    private void getComments(final EventViewHolder viewHolder, final Event currentEvent) {
        final Context mContext = viewHolder.tvLikes.getContext();
        final int event_id = currentEvent.getEvent_id();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_GETCOMMENTCOUNT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject commentObject = new JSONObject(response);

                    if (!commentObject.getBoolean("error")) {
                        JSONArray jsonArray = commentObject.getJSONArray("comments");
                        JSONObject o = jsonArray.getJSONObject(0);

                        int comment = o.getInt("comments");


                        currentEvent.setCommentCount(comment);

                        viewHolder.tvComments.setText(Integer.toString(currentEvent.getCommentCount()));

                    } else {

                        viewHolder.tvComments.setText(Integer.toString(0));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("event_id", Integer.toString(event_id));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    @Override
    public Filter getFilter() {
        return eventFilter;
    }

    public Filter eventFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Event> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(eventListFull);
            } else {

                String filterPattern = constraint.toString().toLowerCase().trim();


                // Szűrés eventre név / leírás / szervező / hely alapján
                for (Event event : eventListFull) {
                    if (event.getEventname().toLowerCase().contains(filterPattern) || event.getDescription().toLowerCase().contains(filterPattern) ||
                            event.getOrganiser().toLowerCase().contains(filterPattern) || event.getLocation().toLowerCase().contains(filterPattern)
                            || event.getMain_category().toLowerCase().contains(filterPattern) || event.getSide_category().toLowerCase().contains(filterPattern)) {
                        filteredList.add(event);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            eventList.clear();
            eventList.addAll((List) results.values);

            Collections.sort(eventList, Event.BY_PRIOR);
            notifyDataSetChanged();
        }
    };

}