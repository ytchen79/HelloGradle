package com.cyt.utils.entity;

import com.cyt.utils.consts.ResultCodeEnum;
import com.cyt.utils.consts.ResultTitleEnum;

import java.util.HashMap;

/**
 * Created by cyt on 2017/12/23.
 */
public class ResultMap extends HashMap<String, Object> {

    public ResultMap() {
        put(ResultTitleEnum.RETURN_CODE.value(), ResultCodeEnum.ERROR.value());
        put(ResultTitleEnum.TIMESTAMP.value(), String.valueOf(System.currentTimeMillis()));
        put(ResultTitleEnum.RETURN_MSG.value(), ResultCodeEnum.SUCCESS.value());
    }

    public ResultMap(String resultCode, String resultMsg) {
        put(ResultTitleEnum.RETURN_CODE.value(), resultCode);
        put(ResultTitleEnum.TIMESTAMP.value(), String.valueOf(System.currentTimeMillis()));
        put(ResultTitleEnum.RETURN_MSG.value(), resultMsg);
    }

    public String getResultCode() {
        Object resultCode = get(ResultTitleEnum.RETURN_CODE.value());
        return resultCode == null ? null : (String) resultCode;
    }

    public void setResultCode(String resultCode) {
        put(ResultTitleEnum.RETURN_CODE.value(), resultCode);
    }

    public String getResultMsg() {
        Object resultMsg = get(ResultTitleEnum.RETURN_MSG.value());
        return resultMsg == null ? null : (String) resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        put(ResultTitleEnum.RETURN_MSG.value(), resultMsg);
    }

    public Object getResultData() {
        return get(ResultTitleEnum.RETURN_DATA.value());
    }

    public void setResultData(Object resultData) {
        put(ResultTitleEnum.RETURN_DATA.value(), resultData);
    }

    public boolean isSuccess() {
        return ResultCodeEnum.SUCCESS.value().equals(get(ResultTitleEnum.RETURN_CODE.value()));
    }

}
