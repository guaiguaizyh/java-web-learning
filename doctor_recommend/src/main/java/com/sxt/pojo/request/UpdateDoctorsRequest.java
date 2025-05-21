package com.sxt.pojo.request;

import com.sxt.pojo.DoctorExpertise;
import lombok.Data;
import java.util.List;

@Data
public class UpdateDoctorsRequest {
    private List<DoctorExpertise> doctors;
} 