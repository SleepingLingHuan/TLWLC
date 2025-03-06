#ifndef HASHMAP_H
#define HASHMAP_H

#include "ExpandableLinkedHashTable.h"
#include <set>
#include <utility>

template <typename K, typename V>
class HashMap {
private:
    ExpandableLinkedHashTable<K, V> hashTable;

public:
    HashMap(int initialSize = 16, double maxLoadFactor = 0.7); // 构造函数
    V& getValue(const K& key); // 获取值
    std::set<K> keySet(); // 返回键的集合
    bool containsKey(const K& key) const; // 检查键是否存在
    void Insert(const std::pair<K, V>& k_v); // 插入或更新键值对
    V& Remove(const K& key); // 删除键
    V& Remove(const K& key, const V& val); // 删除键值对
    void Clear(); // 清除映射
    int getSize() const; // 获取映射的总数
    ~HashMap(); // 析构函数
    void resizeTable(); // 调整散列表大小
};

#endif // HASHMAP_H
