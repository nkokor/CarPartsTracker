package com.formula.parts.tracker.shared.dto.pkg;

import com.formula.parts.tracker.shared.dto.carpart.CarPartResponse;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String status;
    private Long shipmentId;
    private Long departureStorageId;
    private Long destinationStorageId;

    private List<CarPartResponse> carParts;

}
