package fi.haagahelia.eventmanager.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "eve_address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "add_no")
    private Long id;

    @Column(name = "add_number", nullable = false)
    private String number;

    @Column(name = "add_street", nullable = false)
    private String street;

    @Column(name = "add_postcode", nullable = false)
    private String postcode;

    @Column(name = "add_city", nullable = false)
    private String city;

    /*------------------------------------------------ RELATIONS -----------------------------------------------------*/
    @ManyToOne
    @JoinColumn(name = "add_cou_no")
    private Country country;

    //Empty constructor for JPA needs

    public Address() {
    }

    public Address(String number, String street, String postcode, String city, Country country) {
        this.number = number;
        this.street = street;
        this.postcode = postcode;
        this.city = city;
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id) && Objects.equals(number, address.number) &&
                Objects.equals(street, address.street) && Objects.equals(postcode, address.postcode)
                && Objects.equals(city, address.city) && Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, street, postcode, city, country);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", street='" + street + '\'' +
                ", postcode='" + postcode + '\'' +
                ", city='" + city + '\'' +
                ", country=" + country +
                '}';
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
