#ifndef POINT_H
#define POINT_H

#include <stdint.h>

typedef struct {
    float x;
    float y;
} point_t;

typedef struct {
    point_t *data;
    size_t count;
} point_array_t;

__declspec(dllexport) point_t *point_new(float x, float y);

__declspec(dllexport) void point_add_mut(point_t *to, point_t b);

__declspec(dllexport) point_t point_add(point_t a, point_t b);

__declspec(dllexport) point_array_t *point_array_new(size_t count);

#endif POINT_H