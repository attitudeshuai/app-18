package com.babygearpass.dto.wishlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistMatchStatusRequest {

    @NotBlank
    @Pattern(regexp = "^(Pending|Accepted|Rejected)$", message = "状态只能是Pending、Accepted或Rejected")
    private String status;

    private Boolean isAccepted;
}
