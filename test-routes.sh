#!/bin/bash

# 路由功能测试脚本
echo "=== Contract Gateway 路由功能测试 ==="
echo "网关地址: http://localhost:9090"
echo ""

# 测试路由管理API
echo "1. 测试获取所有路由定义"
curl -s http://localhost:9090/admin/routes | head -20
echo ""
echo ""

echo "2. 测试健康检查"
curl -s http://localhost:9090/admin/health
echo ""
echo ""

echo "3. 测试监控端点"
curl -s http://localhost:9090/actuator/health
echo ""
echo ""

echo "4. 测试合同管理服务路由 (/cm/**)"
echo "   请求: http://localhost:9090/cm/test"
echo "   预期: 路由到 contract-management-service 服务"
curl -s -i http://localhost:9090/cm/test -w "\n" | head -10
echo ""

echo "5. 测试合同审查引擎路由 (/cre/**)"
echo "   请求: http://localhost:9090/cre/test"
echo "   预期: 路由到 contract-review-engine 服务"
curl -s -i http://localhost:9090/cre/test -w "\n" | head -10
echo ""

echo "6. 测试文件存储服务路由 (/cfs/**)"
echo "   请求: http://localhost:9090/cfs/test"
echo "   预期: 路由到 contract-file-storage-service 服务"
curl -s -i http://localhost:9090/cfs/test -w "\n" | head -10
echo ""

echo "7. 测试AI服务路由 (/cai/**)"
echo "   请求: http://localhost:9090/cai/test"
echo "   预期: 路由到 contract-ai-service 服务"
curl -s -i http://localhost:9090/cai/test -w "\n" | head -10
echo ""

echo "8. 测试API版本兼容路由 (/api/v1/cm/**)"
echo "   请求: http://localhost:9090/api/v1/cm/test"
echo "   预期: 路由到 contract-management-service 服务"
curl -s -i http://localhost:9090/api/v1/cm/test -w "\n" | head -10
echo ""

echo "9. 测试文件上传专用路由 (/cfs/upload/**)"
echo "   请求: http://localhost:9090/cfs/upload/test"
echo "   预期: 路由到 contract-file-storage-service 服务 (上传模式)"
curl -s -i http://localhost:9090/cfs/upload/test -w "\n" | head -10
echo ""

echo "10. 测试AI推理专用路由 (/cai/inference/**)"
echo "    请求: http://localhost:9090/cai/inference/test"
echo "    预期: 路由到 contract-ai-service 服务 (推理模式)"
curl -s -i http://localhost:9090/cai/inference/test -w "\n" | head -10
echo ""

echo "=== 路径映射说明 ==="
echo "cm  -> contract-management-service (Contract Management)"
echo "cre -> contract-review-engine (Contract Review Engine)"
echo "cfs -> contract-file-storage-service (Contract File Storage)"
echo "cai -> contract-ai-service (Contract AI Service)"
echo ""

echo "=== 测试完成 ==="
echo "注意: 如果后端服务未启动，会返回 503 Service Unavailable"