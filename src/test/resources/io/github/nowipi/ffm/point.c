#include "point.h"

void point_add(point_t *to, point_t value) {
    to->x += value.x;
    to->y += value.y;
}