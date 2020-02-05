package hu.vtominator.edu.Controller;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hu.vtominator.edu.Model.Comment;
import hu.vtominator.edu.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private static final String TAG = "EventAdapter";
    private List<Comment> commentList;

    private OnItemClickListener mListener;


    public interface OnItemClickListener {

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername, tvComment;


        public CommentViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvComment = itemView.findViewById(R.id.tvComment);

        }
    }

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;

    }

    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_comment, viewGroup, false);
        CommentViewHolder commentViewHolder = new CommentViewHolder(view, mListener);

        return commentViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder viewHolder, int i) {
        final Comment currentComment = commentList.get(i);

        viewHolder.tvUsername.setText(currentComment.getUsername());
        viewHolder.tvComment.setText(currentComment.getComment());

    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }


}