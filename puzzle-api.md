``游戏分为几个阶段，登陆>组队>拼图

`⚡` 事件，Server->Client

 `/` 普通HTTP请求

##/login

```
{
    username: string, //名字
    department: string, //部门
}
```

返回一个鉴权token

## /group

### ⚡️ init

用户所在的组，如果是第一次进入则创建新组，返回：

```json
{"group_id": 1234}
```

### /view
 
```json
{
    "users": [
    	{
            "username": "欧阳亚坤",
            "department": "设计"
        }
    ]
}
```

用户查看组

### /join

```json
{"group_id": 1234}
```

用户加入别的组

### ⚡️ new

组接收新用户

```json
{
    "username": "欧阳亚坤",
    "department": "设计"
}
```

### ⚡️ groupTimeout

组队时间已过，返回组队结果，进入下一阶段



### / board

```json
{
    "id": 1, //拼图类型
    "blocks": [2, 4, 6] //我的拼图块
}
```

拼图初始化

### /⚡️ set

```json
{
    "slot": 1,
   	"block": 0 // 1-9 拼图块 0 空
}
```

### ⚡️ finish

完成拼图

### ⚡️ puzzleTimeout

拼图时间已过



***下面是后台 API***

### /groups

返回全部组

### ⚡️ group_changed

组别发生改变

```json
{
    "added": [],
    "removed": []
}
```

### ⚡️ group_finished

有组别完成拼图

```json
{
    "group_id": 1111,
    "rank": 1 //名次
}
```



## /admin

### /flush_all

清除缓存，清除全部数据&用户



### /start_game

开始游戏

```json
{
    "groupTime": 3000,
    "puzzleTime": 3000,
}
```

