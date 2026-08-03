resource "aws_cloudwatch_log_group" "app" {
  count = var.create_ecs_service ? 1 : 0

  name              = "/ecs/${local.name_prefix}"
  retention_in_days = 14
}

resource "aws_iam_role" "ecs_execution" {
  count = var.create_ecs_service ? 1 : 0

  name = "${local.name_prefix}-ecs-execution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution" {
  count = var.create_ecs_service ? 1 : 0

  role       = aws_iam_role.ecs_execution[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_execution_secrets" {
  count = var.create_ecs_service ? 1 : 0

  name = "${local.name_prefix}-ecs-secrets"
  role = aws_iam_role.ecs_execution[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "secretsmanager:GetSecretValue"
      ]
      Resource = [aws_secretsmanager_secret.app.arn]
    }]
  })
}

resource "aws_iam_role" "ecs_task" {
  count = var.create_ecs_service ? 1 : 0

  name = "${local.name_prefix}-ecs-task"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_ecs_cluster" "main" {
  count = var.create_ecs_service ? 1 : 0

  name = "${local.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "disabled"
  }
}

resource "aws_ecs_task_definition" "app" {
  count = var.create_ecs_service ? 1 : 0

  family                   = "${local.name_prefix}-app"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.ecs_execution[0].arn
  task_role_arn            = aws_iam_role.ecs_task[0].arn

  container_definitions = jsonencode([
    {
      name      = "api"
      image     = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"
      essential = true
      portMappings = [{
        containerPort = var.container_port
        hostPort      = var.container_port
        protocol      = "tcp"
      }]
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "DB_USER", value = var.db_username },
        { name = "DB_URL", value = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${var.db_name}" },
        { name = "CORS_ALLOWED_ORIGINS", value = var.cors_allowed_origins },
        { name = "SEED_DEMO_USERS", value = tostring(var.seed_demo_users) },
        { name = "ALLOW_REGISTRATION", value = tostring(var.allow_registration) }
      ]
      secrets = [
        {
          name      = "DB_PASSWORD"
          valueFrom = "${aws_secretsmanager_secret.app.arn}:DB_PASSWORD::"
        },
        {
          name      = "JWT_SECRET"
          valueFrom = "${aws_secretsmanager_secret.app.arn}:JWT_SECRET::"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app[0].name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "api"
        }
      }
      # ALB health check targets /actuator/health (Alpine JRE has no curl/wget by default)
    }
  ])
}

resource "aws_lb" "app" {
  count = var.create_ecs_service ? 1 : 0

  name               = substr("${local.name_prefix}-alb", 0, 32)
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb[0].id]
  subnets            = aws_subnet.public[*].id
}

resource "aws_lb_target_group" "app" {
  count = var.create_ecs_service ? 1 : 0

  name        = substr("${local.name_prefix}-tg", 0, 32)
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = "/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }
}

resource "aws_lb_listener" "http" {
  count = var.create_ecs_service ? 1 : 0

  load_balancer_arn = aws_lb.app[0].arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app[0].arn
  }
}

resource "aws_ecs_service" "app" {
  count = var.create_ecs_service ? 1 : 0

  name            = "${local.name_prefix}-svc"
  cluster         = aws_ecs_cluster.main[0].id
  task_definition = aws_ecs_task_definition.app[0].arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.assign_public_ip ? aws_subnet.public[*].id : aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs[0].id]
    assign_public_ip = var.assign_public_ip
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app[0].arn
    container_name   = "api"
    container_port   = var.container_port
  }

  depends_on = [
    aws_lb_listener.http,
    aws_iam_role_policy.ecs_execution_secrets
  ]

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}
