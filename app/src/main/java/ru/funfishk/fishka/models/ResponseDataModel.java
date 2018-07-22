package ru.funfishk.fishka.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ghostman on 10.03.2018.
 */

public class ResponseDataModel {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("file_url")
    @Expose
    private String fileUrl;
    @SerializedName("published_at")
    @Expose
    private String publishedAt;
    @SerializedName("published_at_timestamp")
    @Expose
    private int publishedAtTimestamp;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("tags")
    @Expose
    private List<String> tags = null;
    @SerializedName("comments_count")
    @Expose
    private int commentsCount;
    @SerializedName("likes_count")
    @Expose
    private int likesCount;
    @SerializedName("liked")
    @Expose
    private boolean liked;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public int getPublishedAtTimestamp() {
        return publishedAtTimestamp;
    }

    public void setPublishedAtTimestamp(int publishedAtTimestamp) {
        this.publishedAtTimestamp = publishedAtTimestamp;
    }
}
