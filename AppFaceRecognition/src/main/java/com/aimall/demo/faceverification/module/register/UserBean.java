package com.aimall.demo.faceverification.module.register;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户注册信息
 */
public class UserBean
        implements Parcelable {
	private String name;
	private String identifyNum;//身份证号  目前未使用
	private String sexy;
	private String birthDate;
	private String faceId;
	private String nickName;//称谓：爷爷 奶奶 爸爸 妈妈
	private String openId;//微信

	public String getName() {
		return name == null ? "" : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifyNum() {
		return identifyNum == null ? "" : identifyNum;
	}

	public void setIdentifyNum(String identifyNum) {
		this.identifyNum = identifyNum;
	}

	public String getSexy() {
		return sexy;
	}

	public void setSexy(String sexy) {
		this.sexy = sexy;
	}

	public String getBirthDate() {
		return birthDate == null ? "" : birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public void setBirthDate(int year,int month,int day) {
		this.birthDate = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);
	}


	public String getFaceId() {
		return faceId == null ? "" : faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public String getNickName() {
		return nickName == null ? "" : nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getOpenId() {
		return openId == null ? "" : openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.identifyNum);
		dest.writeString(this.sexy);
		dest.writeString(this.birthDate);
		dest.writeString(this.faceId);
		dest.writeString(this.nickName);
		dest.writeString(this.openId);
	}

	public UserBean() {}

	protected UserBean(Parcel in) {
		this.name = in.readString();
		this.identifyNum = in.readString();
		this.sexy = in.readString();
		this.birthDate = in.readString();
		this.faceId = in.readString();
		this.nickName = in.readString();
		this.openId = in.readString();
	}

	public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {
		@Override
		public UserBean createFromParcel(Parcel source) {
			return new UserBean(source);
		}

		@Override
		public UserBean[] newArray(int size) {
			return new UserBean[size];
		}
	};
}
