package brucehan.product.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private int price;

    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "brand_name", nullable = false, length = 50)
    private String brandName;

    @Column(name = "seller", nullable = false, length = 50)
    private String seller;
}
