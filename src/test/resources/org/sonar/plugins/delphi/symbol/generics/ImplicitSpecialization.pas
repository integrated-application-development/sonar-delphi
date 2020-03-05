unit ImplicitSpecialization;

{This is a sample Delphi file.}

interface

type
  TConsumable = class
    constructor Create; override;
    procedure GetEaten;
    class function Pizza: TConsumable;
  end;

  TConsumer = class
    procedure Test(Consumable: TConsumable);
    procedure Consume<T: TConsumable>(Tasty: T);
  end;

implementation

procedure TConsumer.Consume<T>(Consumable: T);
begin
  Consumable.GetEaten;
end;

procedure TConsumer.Test(Consumable: TConsumable);
begin
  Consume(Consumable);
  Consume(TConsumable.Pizza);
  Consume(TConsumable.Create);
end;

end.