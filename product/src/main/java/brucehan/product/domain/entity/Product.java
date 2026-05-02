package brucehan.product.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "PRODUCT_ID_UNIQUE",
                        columnNames = {"product_id"}
                )
        })
public class Product {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private int price;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "brand_name", nullable = false, length = 50)
    private String brandName;

    @Column(name = "seller", nullable = false, length = 50)
    private String seller;
}
