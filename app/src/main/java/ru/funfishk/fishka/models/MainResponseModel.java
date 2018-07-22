package ru.funfishk.fishka.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ghostman on 10.03.2018.
 */

public class MainResponseModel {
    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("data")
    @Expose
    private List<ResponseDataModel> data = null;
    @SerializedName("meta")
    @Expose
    private ResponseDataModel meta;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<ResponseDataModel> getData() {
        return data;
    }

    public void setData(List<ResponseDataModel> data) {
        this.data = data;
    }

    public ResponseDataModel getMeta() {
        return meta;
    }

    public void setMeta(ResponseDataModel meta) {
        this.meta = meta;
    }
}
