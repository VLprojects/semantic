package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class CouponId(barcode: Barcode) : AbstractId<String>(barcode.digits)
