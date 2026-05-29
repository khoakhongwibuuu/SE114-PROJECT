import json

files = [
    'frontend/app/src/main/java/com/example/carenest/feature/medical/data/model/AppointmentModels.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/data/model/VaccineModels.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/data/remote/AppointmentApi.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/data/remote/VaccineApi.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/data/repository/AppointmentRepository.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/data/repository/VaccineRepository.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/AddAppointmentScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/AddAppointmentViewModel.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/AddVaccineScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/AddVaccineViewModel.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/AppointmentScheduleScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/AppointmentScheduleViewModel.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/AppointmentViewModelFactories.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/VaccineScheduleScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/VaccineScheduleViewModel.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/VaccineViewModelFactories.kt'
]

for f in files:
    try:
        with open(f, 'r', encoding='utf-8') as file:
            content = file.read()
        
        if content.startswith('"') and content.endswith('"'):
            decoded = json.loads(content)
            with open(f, 'w', encoding='utf-8') as file:
                file.write(decoded)
            print(f'Fixed {f}')
        else:
            print(f'Skipped {f}, not json encoded.')
    except Exception as e:
        print(f'Error fixing {f}: {e}')
