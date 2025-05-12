package org.example.messanger;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private String name;
    private String phoneNumber;
    private int id;
    private static int count = 0;

    // I have problem with serialization(server sends the same object - client do not reserialize it and use previous object - so I need to clone for creating a new object)
    public User(User other) {
        this.id = other.id;
        this.name = other.name;
        this.phoneNumber = other.phoneNumber;
    }

    public User(String name, String phoneNumber, int id) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.id = id;
        System.out.println("User created: name=" + name + ", id =" + id + ", phoneNumber = " + phoneNumber);
    }
    public User(String name, int id) {
        this.name = name;
        phoneNumber = "0";
        this.id = id;
        System.out.println("User created: name=" + name + ", id =" + id);
    }
    public User(String name) {
        this.name = name;
        phoneNumber = "0";
        id = 0;
        System.out.println("User created: name=" + name + ", local_id = " + id + ", count=" + count);
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null || this.getClass() != obj.getClass()) return false;
        //comparing IDs
        else if(((User) obj).getId() == this.getId()) return true;
        else return false;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id); // Use Objects.hash() method
    }
}

