package pro.vlprojects.semantic.ddd.spring.data.unit

import org.springframework.data.util.ProxyUtils

class ItemProxyDetector : ProxyUtils.ProxyDetector {

    override fun getUserType(type: Class<*>) = when (type) {
        ItemProxy::class.java -> Item::class.java
        else -> type
    }
}
