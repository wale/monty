package au.id.wale.monty.db

import io.ebean.Model
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "t_user")
class UserEntity: Model() {
    @Id
    var id: Long? = null
    var timezone: String? = null
}