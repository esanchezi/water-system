# FrontLosLopez

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 16.2.7.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.


🔹 Frontend (Angular)
cd water-system-frontend
npm install
ng build --configuration=production
docker build -t watersystem_frontend .
docker save -o watersystem_frontend.tar watersystem_frontend:latest


🔹 Backend (Java)
cd water-system
./mvn clean package -DskipTests   
docker build -t watersystem_backend .
docker save -o watersystem_backend.tar watersystem_backend:latest

🔹 En Windows
docker load -i watersystem_frontend.tar
docker load -i watersystem_backend.tar

🔹 Ejecuta el sistema
docker-compose up -d

git status 
git add src
git commit -m "fit: add module delegacion"  
git push  

git add tsconfig.json 

git pull
git switch origin/feature/WS6-Delegacion   
git merge origin/feature/WS5-UpdateStatus  
git switch main 
 

ng g m modules/assembly/components/assembly-module  --flat  
ng g c modules/assembly/components/assembly-list  
ng g c modules/assembly/components/assembly-details
ng g c modules/assembly/components/assembly-new-attendance 
-- considerar agregar el modulo en dashboard.module.ts y en router-child.module.ts

ng g c modules/receipt/components/receipt-cancelled

ng g m modules/house/components/house-module  --flat  
ng g c modules/house/pages/house-list  
ng g c modules/house/pages/house-details
ng g c modules/house/pages/house-new-user 
ng g c modules/house/components/house-form
-- considerar agregar el modulo en dashboard.module.ts y en router-child.module.ts, SidenavComponent

 ng g c modules/shared/components/receipt-history


 ng g m modules/person/person-module  --flat 
 ng g c modules/person/pages/person-list
 ng g c modules/person/pages/person-details
 ng g c modules/person/components/person-form

--- 
ng g m modules/group/components/group-module  --flat  
ng g c modules/group/pages/group-list  
ng g c modules/group/pages/group-details