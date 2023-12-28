package cmc.mybatisc.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基本状态更改
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseStatusChange<ID, STATUS extends BaseDict<?>> {
    ID id;
    STATUS status;
}
