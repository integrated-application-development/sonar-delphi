unit ImplicitSpecialization;

interface

type
  TConsumable = class
    constructor Create;
    procedure GetEaten;
    class function Pizza: TConsumable;
  end;

  TConsumer = class
    procedure Test(Consumable: TConsumable); overload;
    procedure Test(Consumables: array of TConsumable); overload;
    procedure Consume<T: TConsumable>(Consumable: T); overload;
    procedure Consume<T: TConsumable>(Consumables: array of T); overload;
  end;

implementation

procedure TConsumer.Consume<T>(Consumable: T);
begin
  Consumable.GetEaten;
end;

procedure TConsumer.Consume<T>(Consumables: array of T);
var
  Consumable: T;
begin
  for Consumable in Consumables do begin
    Consume(Consumable);
  end;
end;

procedure TConsumer.Test(Consumable: TConsumable);
begin
  Consume(Consumable);
  Consume(TConsumable.Pizza);
  Consume(TConsumable.Create);
end;

procedure TConsumer.Test(Consumables: array of TConsumable);
var
  DynamicConsumables: array of TConsumable;
begin
  DynamicConsumables := [TConsumable.Create];

  Consume(Consumables);
  Consume(DynamicConsumables);
  Consume([TConsumable.Pizza]);
end;

end.