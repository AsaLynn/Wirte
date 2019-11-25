package com.yinghuanhang.pdf.parser.db;

import org.greenrobot.greendao.test.AbstractDaoTestLongPk;

import com.yinghuanhang.pdf.parser.db.entity.TxtInfo;
import com.yinghuanhang.pdf.parser.db.TxtInfoDao;

public class TxtInfoTest extends AbstractDaoTestLongPk<TxtInfoDao, TxtInfo> {

    public TxtInfoTest() {
        super(TxtInfoDao.class);
    }

    @Override
    protected TxtInfo createEntity(Long key) {
        TxtInfo entity = new TxtInfo();
        entity.setId(key);
        entity.setPosition();
        return entity;
    }

}
