package com.yinghuanhang.pdf.parser.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by think on 2018/2/6.
 */

@Entity(nameInDb = "txt_tb", createInDb = true, generateConstructors = true, generateGettersSetters = true)
public class TxtInfo {
    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "name")
    private String name;

    @Property(nameInDb = "path")
    private String path;

    @Property(nameInDb = "position")
    private int position;

    @Property(nameInDb = "isFirstRead")
    private boolean isFirstRead;

    @Generated(hash = 773439651)
    public TxtInfo(Long id, String name, String path, int position, boolean isFirstRead) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.position = position;
        this.isFirstRead = isFirstRead;
    }

    @Generated(hash = 236478883)
    public TxtInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "TxtInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", position=" + position +
                '}';
    }

    public boolean getIsFirstRead() {
        return this.isFirstRead;
    }

    public void setIsFirstRead(boolean isFirstRead) {
        this.isFirstRead = isFirstRead;
    }
}
