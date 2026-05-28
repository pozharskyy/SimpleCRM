package de.vp.personmanager;
public class Person {

    private int id;
    private String name;
    private String lastname;
    private String street;
    private String postalCode;
    private String city;
    private String country;

    public Person() {}

    public Person(String name, String lastname, String street,
                  String postalCode, String city, String country) {
        this.name       = name;
        this.lastname   = lastname;
        this.street     = street;
        this.postalCode = postalCode;
        this.city       = city;
        this.country    = country;
    }

    // Getters
    public int getId()            { return id; }
    public String getName()       { return name; }
    public String getLastname()   { return lastname; }
    public String getStreet()     { return street; }
    public String getPostalCode() { return postalCode; }
    public String getCity()       { return city; }
    public String getCountry()    { return country; }

    // Setters
    public void setId(int id)                    { this.id = id; }
    public void setName(String name)             { this.name = name; }
    public void setLastname(String lastname)     { this.lastname = lastname; }
    public void setStreet(String street)         { this.street = street; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setCity(String city)             { this.city = city; }
    public void setCountry(String country)       { this.country = country; }
}
