#include <stdlib.h>

typedef struct {
    int *data;
    size_t count;
} array_t;

__declspec(dllexport) array_t *array_new(size_t elementCount);

array_t *array_new(size_t elementCount) {
    array_t *array = (array_t *) malloc(sizeof(array_t));
    array->data = (int *) calloc(elementCount, sizeof(int));
    array->count = elementCount;
    return array;
}

