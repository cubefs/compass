package com.oppo.cloud.portal.dao;

import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.model.Blocklist;

public interface BlocklistExtendMapper extends BlocklistMapper {
    int save(Blocklist record);
}
